package kr.allcll.backend.domain.basket.star;

import static kr.allcll.backend.fixture.SubjectFixture.createSubject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kr.allcll.backend.domain.basket.BasketRepository;
import kr.allcll.backend.domain.basket.star.dto.StarredSubjectIdsResponse;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.semester.Semester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class StarServiceTest {

    private static final String TOKEN = "token";

    @Autowired
    private StarService starService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private StarRepository starRepository;

    @Autowired
    private PinRepository pinRepository;

    @BeforeEach
    void setUp() {
        basketRepository.deleteAllInBatch();
        pinRepository.deleteAllInBatch();
        starRepository.deleteAllInBatch();
        subjectRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("즐겨찾기 50개 미만일 경우 정상 등록을 검증한다.")
    void addStarOnProject() {
        // given
        int expected = 50;
        Subject starSubject = createSubject("컴퓨터구조", "003278", "005", "김보예");
        saveSubjectsAndStars();
        subjectRepository.save(starSubject);
        starService.addStarOnSubject(starSubject.getId(), TOKEN);

        // when
        List<Star> result = starRepository.findAllByToken(TOKEN, Semester.now());

        // then
        assertThat(result).hasSize(expected);
    }

    private void saveSubjectsAndStars() {
        for (int i = 0; i < 49; i++) {
            Subject subject = createSubject("컴퓨터구조", "003278", "001", "김보예");
            subjectRepository.save(subject);
            Star star = new Star(TOKEN, subject);
            starRepository.save(star);
        }
    }

    @Test
    @DisplayName("즐겨찾기가 50개 이상일 경우 예외를 검증한다.")
    void canNotAddStarOnSubject() {
        // given
        int MAX_STAR_NUMBER = 50;
        Subject overCountSubject = createSubject("컴퓨터구조", "003278", "005", "김수민");
        subjectRepository.save(overCountSubject);
        saveSubjectsAndMaxStars();

        // when, then
        assertThatThrownBy(() -> starService.addStarOnSubject(overCountSubject.getId(), TOKEN))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(String.format(AllcllErrorCode.STAR_LIMIT_EXCEEDED.getMessage(), MAX_STAR_NUMBER));
    }

    private void saveSubjectsAndMaxStars() {
        for (int i = 0; i < 50; i++) {
            Subject subject = createSubject("컴퓨터구조", "003278", "001", "김보예");
            subjectRepository.save(subject);
            Star star = new Star(TOKEN, subject);
            starRepository.save(star);
        }
    }

    @Test
    @DisplayName("이미 즐겨찾기 등록된 과목일 경우 예외를 검증한다.")
    void alreadyExistStarSubject() {
        // given
        Subject subject = createSubject("컴퓨터구조", "003278", "001", "김보예");
        subjectRepository.save(subject);
        starRepository.save(new Star(TOKEN, subject));

        // when, then
        assertThatThrownBy(() -> starService.addStarOnSubject(subject.getId(), TOKEN))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(new AllcllException(AllcllErrorCode.DUPLICATE_STAR, "컴퓨터구조").getMessage());
    }

    @Test
    @DisplayName("즐겨찾기의 삭제를 검증한다.")
    void deleteStar() {
        // given
        Subject subject = createSubject("컴퓨터구조", "003278", "001", "김보예");
        subjectRepository.save(subject);
        starRepository.save(new Star(TOKEN, subject));

        // when
        starService.deleteStarOnSubject(subject.getId(), TOKEN);

        // then
        assertThat(starRepository.findAllByToken(TOKEN, Semester.now())).hasSize(0);
    }

    @Test
    @DisplayName("즐겨찾기 되지 않은 않는 과목의 즐겨찾기를 삭제할 때에도 예외가 발생하지 않음을 검증한다.")
    void deleteStarNotExistSubject() {
        // given
        Subject subjectA = createSubject("컴구", "001234", "001", "김보예");
        Subject notStarredSubject = createSubject("컴네", "004321", "001", "김수민");
        subjectRepository.saveAll(List.of(subjectA, notStarredSubject));
        starRepository.save(new Star(TOKEN, subjectA));

        // when, then
        assertThatCode(() -> {
            starService.deleteStarOnSubject(notStarredSubject.getId(), TOKEN);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재 하지 않는 토큰의 즐겨찾기를 삭제할 때에도 예외가 발생하지 않음을 검증한다.")
    void deleteStarNotExistToken() {
        // given
        Subject subjectA = createSubject("컴구", "001234", "001", "김보예");
        subjectRepository.save(subjectA);
        starRepository.save(new Star(TOKEN, subjectA));

        // when, then
        assertThatCode(() -> {
            starService.deleteStarOnSubject(subjectA.getId(), "notExistToken");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("등록된 즐겨찾기가 존재할 때 조회 기능을 테스트한다.")
    void retrieveExisStars() {
        // given
        int expectedSize = 1;
        Subject subject = createSubject("컴퓨터구조", "003278", "001", "김보예");
        subjectRepository.save(subject);
        starRepository.save(new Star(TOKEN, subject));

        // when
        StarredSubjectIdsResponse response = starService.retrieveStars(TOKEN);

        // then
        assertThat(response.subjects()).hasSize(expectedSize);
    }

    @Test
    @DisplayName("등록된 즐겨찾기가 존재할 때 예외가 발생하지 않음을 검증한다.")
    void retrieveStars() {
        // given
        int expectedSize = 0;

        // when
        StarredSubjectIdsResponse response = starService.retrieveStars(TOKEN);

        // then
        assertThat(response.subjects()).hasSize(expectedSize);
    }
}
