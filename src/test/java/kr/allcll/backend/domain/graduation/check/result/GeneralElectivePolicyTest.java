package kr.allcll.backend.domain.graduation.check.result;

import static org.assertj.core.api.Assertions.assertThat;

import kr.allcll.backend.domain.graduation.credit.CategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GeneralElectivePolicyTest {

    private static final String SEJONG_CYBER_CURI_NO = "500001";
    private static final String SEJONG_CYBER_EXCEPTION_CURI_NO = "501335";

    private final GeneralElectivePolicy generalElectivePolicy = new GeneralElectivePolicy();

    @Test
    @DisplayName("교양선택이 아니면 세사대 과목이라도 제외하지 않는다.")
    void shouldExcludeFromGeneralElective_notGeneralElective_false() {
        // given
        int admissionYear = 2021;
        CategoryType categoryType = CategoryType.ACADEMIC_BASIC;
        String curiNo = SEJONG_CYBER_CURI_NO;

        // when
        boolean result = generalElectivePolicy.shouldExcludeFromGeneralElective(admissionYear, categoryType, curiNo);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("22학번 이상은 교양선택에서 세사대 과목을 제외하지 않는다.")
    void shouldExcludeFromGeneralElective_admissionYear2022OrLater_false() {
        // given
        int admissionYear = 2023;
        CategoryType categoryType = CategoryType.GENERAL_ELECTIVE;
        String curiNo = SEJONG_CYBER_CURI_NO;

        // when
        boolean result = generalElectivePolicy.shouldExcludeFromGeneralElective(admissionYear, categoryType, curiNo);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("21학번 이전 교양선택에서 학수번호가 5로 시작하면 세사대 과목으로 제외한다.")
    void shouldExcludeFromGeneralElective_before2022_sejongCyber_true() {
        // given
        int admissionYear = 2021;
        CategoryType categoryType = CategoryType.GENERAL_ELECTIVE;
        String curiNo = SEJONG_CYBER_CURI_NO;

        // when
        boolean result = generalElectivePolicy.shouldExcludeFromGeneralElective(admissionYear, categoryType, curiNo);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("세사대 예외 과목은 21학번 이전이어도 교양선택에서 제외하지 않는다.")
    void shouldExcludeFromGeneralElective_exceptionCourse_false() {
        // given
        int admissionYear = 2021;
        CategoryType categoryType = CategoryType.GENERAL_ELECTIVE;
        String curiNo = SEJONG_CYBER_EXCEPTION_CURI_NO;

        // when
        boolean result = generalElectivePolicy.shouldExcludeFromGeneralElective(admissionYear, categoryType, curiNo);

        // then
        assertThat(result).isFalse();
    }
}
