package kr.allcll.backend.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import kr.allcll.backend.AllcllBackendApplication;
import kr.allcll.backend.domain.seat.SeatStorage;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.fixture.SubjectFixture;
import kr.allcll.crawler.seat.ChangeStatus;
import kr.allcll.crawler.seat.ChangeSubjectsResponse;
import kr.allcll.crawler.seat.ChangedSubjectBuffer;
import kr.allcll.crawler.seat.PinSubjectUpdateRequest;
import kr.allcll.crawler.seat.PinSubjectUpdateRequest.PinSubject;
import kr.allcll.crawler.seat.TargetSubjectStorage;
import kr.allcll.crawler.subject.CrawlerSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AllcllBackendApplication.class)
class ExternalClientTest {

    @Autowired
    private ExternalClient externalClient;

    @Autowired
    private ChangedSubjectBuffer changedSubjectBuffer;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SeatStorage seatStorage;

    @Autowired
    private TargetSubjectStorage targetSubjectStorage;

    @AfterEach
    void tearDown() {
        seatStorage.clear();
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
    }

    @Test
    @DisplayName("변경 감지 과목들의 정상 수신을 확인한다.")
    void getAllTargetSubjects() {
        // given
        Subject subjectA = subjectRepository
            .save(SubjectFixture.createNonMajorSubject("차와문화", "123456", "001", "김보예"));
        Subject subjectB = subjectRepository
            .save(SubjectFixture.createNonMajorSubject("차와문화", "123456", "002", "김보예"));
        changedSubjectBuffer.add(new ChangeSubjectsResponse(subjectA.getId(), ChangeStatus.IN, 10));
        changedSubjectBuffer.add(new ChangeSubjectsResponse(subjectB.getId(), ChangeStatus.IN, 15));

        // when
        externalClient.getAllTargetSubjects();
        List<SeatDto> allSeatDtos = seatStorage.getAll();

        // then
        assertThat(allSeatDtos).hasSize(2)
            .extracting(SeatDto::getSubject, SeatDto::getSeatCount)
            .containsExactly(
                tuple(subjectA, 10),
                tuple(subjectB, 15)
            );
    }
}
