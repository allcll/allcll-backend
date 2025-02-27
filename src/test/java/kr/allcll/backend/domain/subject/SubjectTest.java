package kr.allcll.backend.domain.subject;

import static org.assertj.core.api.Assertions.assertThat;

import kr.allcll.backend.fixture.SubjectFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SubjectTest {

    @DisplayName("비전공 과목이 맞는지 검증한다.")
    @Test
    void isNonMajorTest() {
        Subject subject = SubjectFixture.createNonMajorSubject(1L, "사고와표현1", "009352", "001", "김보예");

        assertThat(subject.isNonMajor()).isTrue();
    }

    @DisplayName("비전공 과목이 아닌지 검증한다.")
    @Test
    void isNonMajorTest2() {
        Subject subject = SubjectFixture.createMajorSubject(1L, "컴구", "009352", "001", "김보예");

        assertThat(subject.isNonMajor()).isFalse();
    }
}
