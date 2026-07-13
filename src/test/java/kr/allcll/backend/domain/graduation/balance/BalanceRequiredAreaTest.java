package kr.allcll.backend.domain.graduation.balance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class BalanceRequiredAreaTest {

    @Test
    @DisplayName("선택영역 문자열은 ENUM으로 정상 변환된다.")
    void fromSelectedAreaNatureScience() {
        // given
        String selectedArea = "자연과과학";

        // when
        BalanceRequiredArea result = BalanceRequiredArea.fromSelectedArea(selectedArea);

        // then
        assertThat(result).isEqualTo(BalanceRequiredArea.NATURE_SCIENCE);
    }

    @ParameterizedTest
    @CsvSource({
        "역사와사상, HISTORY_THOUGHT",
        "경제와사회, ECONOMY_SOCIETY",
        "문화와예술, CULTURE_ARTS",
        "융합과창의, CONVERGENCE_AND_CREATIVITY",
        "자연과 과학, NATURE_SCIENCE"
    })
    @DisplayName("성적표 선택영역 문자열은 공백을 제거한 편람 영역명으로 매칭된다.")
    void fromSelectedAreaCanonicalNames(String selectedArea, BalanceRequiredArea expected) {
        // when
        BalanceRequiredArea result = BalanceRequiredArea.fromSelectedArea(selectedArea);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "존재하지않는영역"})
    @DisplayName("빈 값이나 균형교양 영역이 아닌 문자열은 null을 반환한다")
    void fromSelectedAreaUnknown(String selectedArea) {
        // when
        BalanceRequiredArea result = BalanceRequiredArea.fromSelectedArea(selectedArea);

        // then
        assertThat(result).isNull();
    }
}
