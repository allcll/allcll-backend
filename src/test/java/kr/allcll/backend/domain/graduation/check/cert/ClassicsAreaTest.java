package kr.allcll.backend.domain.graduation.check.cert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ClassicsArea enum 테스트")
class ClassicsAreaTest {

    @DisplayName("라벨로 고전독서 영역을 찾을 수 있다")
    @ParameterizedTest
    @CsvSource({
        "서양의 역사와 사상, WESTERN",
        "동양의 역사와 사상, EASTERN",
        "동·서양의 문학, EASTERN_AND_WESTERN",
        "과학 사상, SCIENCE"
    })
    void findByLabel_success(String label, ClassicsArea expected) {
        // when
        Optional<ClassicsArea> result = ClassicsArea.findByLabel(label);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
    }

    @DisplayName("라벨에 영역 이름이 포함되어 있으면 찾을 수 있다")
    @Test
    void findByLabel_containsLabel() {
        // given
        String label = "영역: 서양의 역사와 사상 (필수)";

        // when
        Optional<ClassicsArea> result = ClassicsArea.findByLabel(label);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(ClassicsArea.WESTERN);
    }

    @DisplayName("알 수 없는 라벨인 경우 빈 Optional을 반환한다")
    @Test
    void findByLabel_unknownLabel() {
        // given
        String unknownLabel = "알 수 없는 영역";

        // when
        Optional<ClassicsArea> result = ClassicsArea.findByLabel(unknownLabel);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("각 영역의 한글 이름을 확인할 수 있다")
    @Test
    void getKoreanName() {
        // when & then
        assertThat(ClassicsArea.WESTERN.getKoreanName()).isEqualTo("서양의 역사와 사상");
        assertThat(ClassicsArea.EASTERN.getKoreanName()).isEqualTo("동양의 역사와 사상");
        assertThat(ClassicsArea.EASTERN_AND_WESTERN.getKoreanName()).isEqualTo("동·서양의 문학");
        assertThat(ClassicsArea.SCIENCE.getKoreanName()).isEqualTo("과학 사상");
    }

    @DisplayName("각 영역의 최대 인정 권수를 확인할 수 있다")
    @Test
    void getMaxRecognizedCount() {
        // when & then
        assertThat(ClassicsArea.WESTERN.getMaxRecognizedCount()).isEqualTo(4);
        assertThat(ClassicsArea.EASTERN.getMaxRecognizedCount()).isEqualTo(2);
        assertThat(ClassicsArea.EASTERN_AND_WESTERN.getMaxRecognizedCount()).isEqualTo(3);
        assertThat(ClassicsArea.SCIENCE.getMaxRecognizedCount()).isEqualTo(1);
    }

    @DisplayName("실제 권수가 최대 인정 권수 이하이면 그대로 반환한다")
    @Test
    void getRecognizedCount_withinLimit() {
        // when & then
        assertThat(ClassicsArea.WESTERN.getRecognizedCount(3)).isEqualTo(3);
        assertThat(ClassicsArea.EASTERN.getRecognizedCount(1)).isEqualTo(1);
        assertThat(ClassicsArea.EASTERN_AND_WESTERN.getRecognizedCount(2)).isEqualTo(2);
        assertThat(ClassicsArea.SCIENCE.getRecognizedCount(1)).isEqualTo(1);
    }

    @DisplayName("실제 권수가 최대 인정 권수를 초과하면 최대값으로 제한한다")
    @Test
    void getRecognizedCount_exceedsLimit() {
        // given
        int westernActual = 5;  // 최대 4권
        int easternActual = 3;  // 최대 2권
        int literatureActual = 4;  // 최대 3권
        int scienceActual = 2;  // 최대 1권

        // when & then
        assertThat(ClassicsArea.WESTERN.getRecognizedCount(westernActual)).isEqualTo(4);
        assertThat(ClassicsArea.EASTERN.getRecognizedCount(easternActual)).isEqualTo(2);
        assertThat(ClassicsArea.EASTERN_AND_WESTERN.getRecognizedCount(literatureActual)).isEqualTo(3);
        assertThat(ClassicsArea.SCIENCE.getRecognizedCount(scienceActual)).isEqualTo(1);
    }

    @DisplayName("실제 권수가 0이면 0을 반환한다")
    @Test
    void getRecognizedCount_zero() {
        // when & then
        assertThat(ClassicsArea.WESTERN.getRecognizedCount(0)).isZero();
        assertThat(ClassicsArea.EASTERN.getRecognizedCount(0)).isZero();
        assertThat(ClassicsArea.EASTERN_AND_WESTERN.getRecognizedCount(0)).isZero();
        assertThat(ClassicsArea.SCIENCE.getRecognizedCount(0)).isZero();
    }
}