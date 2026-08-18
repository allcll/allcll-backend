package kr.allcll.backend.admin.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.support.semester.Semester;
import kr.allcll.crawler.subject.CrawlerSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TargetSubjectStorageTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private CrawlerSubject crawlerSubjectA;

    @Mock
    private CrawlerSubject crawlerSubjectB;

    @Mock
    private Subject subjectA;

    @Mock
    private Subject subjectB;

    private TargetSubjectStorage targetSubjectStorage;

    @BeforeEach
    void setUp() {
        targetSubjectStorage = new TargetSubjectStorage(subjectRepository);

        given(crawlerSubjectA.getId()).willReturn(1L);
        given(crawlerSubjectB.getId()).willReturn(2L);
        given(subjectA.getId()).willReturn(1L);
        given(subjectB.getId()).willReturn(2L);
        given(subjectA.isDeleted()).willReturn(false);
        given(subjectB.isDeleted()).willReturn(false);
        given(subjectA.getSemesterAt()).willReturn(Semester.getCurrentSemester());
        given(subjectB.getSemesterAt()).willReturn(Semester.getCurrentSemester());
    }

    @Test
    @DisplayName("핀 대상이 유지되면 Subject를 다시 조회하지 않고 새 대상만 조회한다.")
    void reusePinSubjectSnapshotAndLoadOnlyNewSubjects() {
        // given
        given(subjectRepository.findAllById(List.of(1L))).willReturn(List.of(subjectA));
        given(subjectRepository.findAllById(List.of(2L))).willReturn(List.of(subjectB));

        // when
        targetSubjectStorage.addPinSubjects(pinSubjects(crawlerSubjectA));
        targetSubjectStorage.addPinSubjects(pinSubjects(crawlerSubjectA));
        targetSubjectStorage.addPinSubjects(pinSubjects(crawlerSubjectA, crawlerSubjectB));

        // then
        verify(subjectRepository).findAllById(List.of(1L));
        verify(subjectRepository).findAllById(List.of(2L));
        assertThat(targetSubjectStorage.getPinSubject(1L)).isSameAs(subjectA);
        assertThat(targetSubjectStorage.getPinSubject(2L)).isSameAs(subjectB);
    }

    private Map<CrawlerSubject, Integer> pinSubjects(CrawlerSubject... crawlerSubjects) {
        Map<CrawlerSubject, Integer> subjects = new LinkedHashMap<>();
        for (CrawlerSubject crawlerSubject : crawlerSubjects) {
            subjects.put(crawlerSubject, 1);
        }
        return subjects;
    }
}
