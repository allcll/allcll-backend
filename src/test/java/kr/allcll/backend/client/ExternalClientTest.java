package kr.allcll.backend.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kr.allcll.backend.AllcllBackendApplication;
import kr.allcll.backend.admin.seat.TargetSubjectStorage;
import kr.allcll.backend.admin.seat.dto.PinSubjectUpdateRequest;
import kr.allcll.backend.admin.seat.dto.PinSubjectUpdateRequest.PinSubject;
import kr.allcll.backend.domain.seat.SeatStorage;
import kr.allcll.backend.domain.seat.pin.Pin;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.fixture.SubjectFixture;
import kr.allcll.backend.support.sse.SseEmitterStorage;
import kr.allcll.crawler.subject.CrawlerSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@SpringBootTest(classes = AllcllBackendApplication.class)
class ExternalClientTest {

    @Autowired
    private ExternalClient externalClient;

    @Autowired
    private ExternalService externalService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private SeatStorage seatStorage;

    @Autowired
    private TargetSubjectStorage targetSubjectStorage;

    @Autowired
    private SseEmitterStorage sseEmitterStorage;

    @AfterEach
    void tearDown() {
        seatStorage.clear();
        pinRepository.deleteAllInBatch();
        subjectRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("전공 과목의 정상적인 전달을 확인한다.")
    void sendPinSubjects() {
        // given
        Subject subjectA = subjectRepository
            .save(SubjectFixture.createSubjectWithDepartmentCode("전공과목1", "654321", "001", "김주환", "010812"));
        Subject subjectB = subjectRepository
            .save(SubjectFixture.createSubjectWithDepartmentCode("전공과목2", "654321", "002", "김주환", "010812"));
        PinSubjectUpdateRequest pinSubjectUpdateRequest = new PinSubjectUpdateRequest(List.of(
            new PinSubject(subjectA.getId(), 1),
            new PinSubject(subjectB.getId(), 2)
        ));

        // when
        externalClient.sendPinSubjects(pinSubjectUpdateRequest);
        List<CrawlerSubject> result = targetSubjectStorage.getTargetSubjects();

        // then
        assertThat(result).hasSize(2)
            .extracting(CrawlerSubject::getId)
            .containsExactly(
                subjectA.getId(),
                subjectB.getId()
            );
        assertThat(targetSubjectStorage.getPinSubject(subjectA.getId()).getId()).isEqualTo(subjectA.getId());

        // 대상 과목이 변경되면 snapshot도 새 대상 기준으로 교체된다.
        externalClient.sendPinSubjects(new PinSubjectUpdateRequest(List.of(new PinSubject(subjectB.getId(), 1))));

        assertThat(targetSubjectStorage.getTargetSubjects())
            .extracting(CrawlerSubject::getId)
            .containsExactly(subjectB.getId());
        assertThat(targetSubjectStorage.getPinSubject(subjectB.getId()).getId()).isEqualTo(subjectB.getId());
    }

    @Test
    @DisplayName("활성 SSE token의 핀 과목만 크롤러 대상에 전달한다.")
    void sendWantPinSubjectsToCrawler() {
        // given
        Subject activeSubject = subjectRepository.save(
            SubjectFixture.createMajorSubject(null, "활성 과목", "000001", "001", "김주환")
        );
        Subject inactiveSubject = subjectRepository.save(
            SubjectFixture.createMajorSubject(null, "비활성 과목", "000002", "001", "김주환")
        );
        String activeToken = "active-token";
        SseEmitter emitter = new SseEmitter();
        sseEmitterStorage.add(activeToken, emitter);
        pinRepository.saveAll(List.of(
            new Pin(activeToken, activeSubject),
            new Pin("inactive-token", inactiveSubject)
        ));

        try {
            // when
            externalService.sendWantPinSubjectIdsToCrawler();

            // then
            assertThat(targetSubjectStorage.getTargetSubjects())
                .extracting(CrawlerSubject::getId)
                .containsExactly(activeSubject.getId());
            assertThat(targetSubjectStorage.getPinSubject(activeSubject.getId()).getId())
                .isEqualTo(activeSubject.getId());
        } finally {
            emitter.complete();
        }
    }
}
