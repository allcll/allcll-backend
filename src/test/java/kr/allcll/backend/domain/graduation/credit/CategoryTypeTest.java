package kr.allcll.backend.domain.graduation.credit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CategoryTypeTest {

    @Test
    @DisplayName("정의된 이수구분과 매핑되지 않는 값이면 null을 반환한다.")
    void fromRaw_whenUnknownCategoryTypeRaw() {
        // given
        String CategoryTypeRaw = "UNKNOWN";
        int admissionYear = 2025;

        // when
        CategoryType result = CategoryType.fromRaw(CategoryTypeRaw, admissionYear);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("엑셀 이수구분 문자열의 앞뒤 공백을 제거한 뒤 매핑한다.")
    void fromRaw_trimsWhitespace() {
        // given
        String CategoryTypeRaw = "   공필   ";
        int admissionYear = 2025;

        // when
        CategoryType result = CategoryType.fromRaw(CategoryTypeRaw, admissionYear);

        // then
        assertThat(result).isEqualTo(CategoryType.COMMON_REQUIRED);
    }

    @Test
    @DisplayName("전기를 제외한 정의된 이수구분의 매핑이 정상 동작한다.")
    void fromRaw() {
        // given
        int admissionYear = 2025;

        //when && then
        assertThat(CategoryType.fromRaw("교필", admissionYear)).isEqualTo(CategoryType.COMMON_REQUIRED);
        assertThat(CategoryType.fromRaw("공필", admissionYear)).isEqualTo(CategoryType.COMMON_REQUIRED);

        assertThat(CategoryType.fromRaw("균필", admissionYear)).isEqualTo(CategoryType.BALANCE_REQUIRED);

        assertThat(CategoryType.fromRaw("기교", admissionYear)).isEqualTo(CategoryType.ACADEMIC_BASIC);
        assertThat(CategoryType.fromRaw("기필", admissionYear)).isEqualTo(CategoryType.ACADEMIC_BASIC);

        assertThat(CategoryType.fromRaw("교선", admissionYear)).isEqualTo(CategoryType.GENERAL_ELECTIVE);
        assertThat(CategoryType.fromRaw("교선1", admissionYear)).isEqualTo(CategoryType.GENERAL_ELECTIVE);
        assertThat(CategoryType.fromRaw("교선2", admissionYear)).isEqualTo(CategoryType.GENERAL_ELECTIVE);

        assertThat(CategoryType.fromRaw("교양", admissionYear)).isEqualTo(CategoryType.GENERAL);

        assertThat(CategoryType.fromRaw("전필", admissionYear)).isEqualTo(CategoryType.MAJOR_REQUIRED);
        assertThat(CategoryType.fromRaw("복필", admissionYear)).isEqualTo(CategoryType.MAJOR_REQUIRED);

        assertThat(CategoryType.fromRaw("전선", admissionYear)).isEqualTo(CategoryType.MAJOR_ELECTIVE);
        assertThat(CategoryType.fromRaw("복선", admissionYear)).isEqualTo(CategoryType.MAJOR_ELECTIVE);
    }

    @Test
    @DisplayName("23학번 이하에서 '전기'는 '기필'로 보정된다.")
    void fromRaw_normalizesMajorBasic_toAcademicBasic_whenAdmissionYearBefore2024() {
        // given
        String CategoryTypeRaw = "전기";

        // when && then
        assertThat(CategoryType.fromRaw(CategoryTypeRaw, 2018)).isEqualTo(CategoryType.ACADEMIC_BASIC);
        assertThat(CategoryType.fromRaw(CategoryTypeRaw, 2019)).isEqualTo(CategoryType.ACADEMIC_BASIC);
        assertThat(CategoryType.fromRaw(CategoryTypeRaw, 2020)).isEqualTo(CategoryType.ACADEMIC_BASIC);
        assertThat(CategoryType.fromRaw(CategoryTypeRaw, 2021)).isEqualTo(CategoryType.ACADEMIC_BASIC);
        assertThat(CategoryType.fromRaw(CategoryTypeRaw, 2022)).isEqualTo(CategoryType.ACADEMIC_BASIC);
        assertThat(CategoryType.fromRaw(CategoryTypeRaw, 2023)).isEqualTo(CategoryType.ACADEMIC_BASIC);
    }

    @Test
    @DisplayName("24학번 이상에서 '전기'는 '전기'로 유지된다")
    void fromRaw_keepsMajorBasic_whenAdmissionYearGte2024() {
        //given
        String CategoryTypeRaw = "전기";

        // when && then
        assertThat(CategoryType.fromRaw(CategoryTypeRaw, 2024)).isEqualTo(CategoryType.MAJOR_BASIC);
        assertThat(CategoryType.fromRaw(CategoryTypeRaw, 2025)).isEqualTo(CategoryType.MAJOR_BASIC);
        assertThat(CategoryType.fromRaw(CategoryTypeRaw, 2026)).isEqualTo(CategoryType.MAJOR_BASIC);
    }

    @Test
    @DisplayName("전필/전선만 전공 카테고리로 판단한다.")
    void isMajorCategory() {
        //when && then
        assertThat(CategoryType.MAJOR_REQUIRED.isMajorCategory()).isTrue();
        assertThat(CategoryType.MAJOR_ELECTIVE.isMajorCategory()).isTrue();
        assertThat(CategoryType.MAJOR_BASIC.isMajorCategory()).isFalse();
        assertThat(CategoryType.ACADEMIC_BASIC.isMajorCategory()).isFalse();
        assertThat(CategoryType.COMMON_REQUIRED.isMajorCategory()).isFalse();
        assertThat(CategoryType.BALANCE_REQUIRED.isMajorCategory()).isFalse();
        assertThat(CategoryType.GENERAL_ELECTIVE.isMajorCategory()).isFalse();
        assertThat(CategoryType.GENERAL.isMajorCategory()).isFalse();
        assertThat(CategoryType.TOTAL_COMPLETION.isMajorCategory()).isFalse();
    }
}
