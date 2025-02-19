package kr.allcll.seatfinder.subject;

import static kr.allcll.seatfinder.support.fixture.SubjectFixture.createSubject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import kr.allcll.seatfinder.pin.PinRepository;
import kr.allcll.seatfinder.star.StarRepository;
import kr.allcll.seatfinder.subject.dto.SubjectResponse;
import kr.allcll.seatfinder.subject.dto.SubjectsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class SubjectServiceTest {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private StarRepository starRepository;

    @Autowired
    private PinRepository pinRepository;

    @BeforeEach
    void setUp() {
        pinRepository.deleteAllInBatch();
        starRepository.deleteAllInBatch();
        subjectRepository.deleteAllInBatch();
        initializeSubjects();
    }

    private void initializeSubjects() {
        subjectRepository.saveAll(
            List.of(
                createSubject("컴퓨터구조", "003278", "001", "유재석"),
                createSubject("운영체제", "003279", "001", "노홍철"),
                createSubject("자료구조", "003280", "001", "하하"),
                createSubject("알고리즘", "003281", "001", "길"),
                createSubject("컴퓨터구조", "003278", "002", "정형돈"),
                createSubject("운영체제", "003279", "002", "나영석"),
                createSubject("자료구조", "003280", "003", "박명수"),
                createSubject("알고리즘", "003281", "004", "전진")
            )
        );
    }

    @Test
    @DisplayName("과목명으로 과목을 조회한다.")
    void findSubjectByQueryTest() {
        SubjectsResponse subjectsResponse = subjectService.findSubjectsByCondition(null, "컴퓨터구조", null, null, null);

        assertThat(subjectsResponse.subjectResponses()).hasSize(2)
            .extracting(
                SubjectResponse::subjectName,
                SubjectResponse::subjectCode,
                SubjectResponse::classCode,
                SubjectResponse::professorName
            )
            .containsExactly(
                tuple("컴퓨터구조", "003278", "001", "유재석"),
                tuple("컴퓨터구조", "003278", "002", "정형돈")
            );
    }

    @Test
    @DisplayName("학수번호, 분반, 교수명으로 과목을 조회한다.")
    void findSubjectByQueryTest2() {
        SubjectsResponse subjectsResponse = subjectService.findSubjectsByCondition(null, null, "003279", "001", "노홍철");

        assertThat(subjectsResponse.subjectResponses()).hasSize(1)
            .extracting(
                SubjectResponse::subjectName,
                SubjectResponse::subjectCode,
                SubjectResponse::classCode,
                SubjectResponse::professorName
            )
            .containsExactly(
                tuple("운영체제", "003279", "001", "노홍철")
            );
    }

    @Test
    @DisplayName("존재하지 않는 조건으로 과목을 조회한다.")
    void findSubjectByQueryTest3() {
        SubjectsResponse subjectsResponse = subjectService.findSubjectsByCondition(100L, null, "003279", "001",
            "유재석");

        assertThat(subjectsResponse.subjectResponses()).hasSize(0);
    }

    @Test
    @DisplayName("전체 과목을 조회한다.")
    void getAllSubjects() {
        // when
        SubjectsResponse allSubjects = subjectService.findSubjectsByCondition(null, null, null, null, null);

        // then
        assertThat(allSubjects.subjectResponses()).hasSize(8);
    }
}
