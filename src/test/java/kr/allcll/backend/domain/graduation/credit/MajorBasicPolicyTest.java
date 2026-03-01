package kr.allcll.backend.domain.graduation.credit;

import static org.assertj.core.api.Assertions.assertThat;

import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.fixture.CompletedCourseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MajorBasicPolicyTest {

    @Autowired
    private MajorBasicPolicy majorBasicPolicy;

    @Test
    @DisplayName("사용자가 이수한 과목의 이수구분이 기준 데이터의 이수구분과 같으면 true를 반환한다.")
    void matchesCriterionCategory_returnTrue_whenSameCategory() {
        // given
        int admissionYear = 2025;
        CompletedCourse course = CompletedCourseFixture.createCompletedCourse(CategoryType.ACADEMIC_BASIC);
        CategoryType criterionCategoryType = CategoryType.ACADEMIC_BASIC;

        // when
        boolean result = majorBasicPolicy.matchesCriterionCategory(admissionYear, course, criterionCategoryType);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("24학번 이상에서 전기는 기필로 보정하지 않는다.")
    void matchesCriterionCategory_returnFalse_whenAdmissionYearGte2024() {
        // given
        int admissionYear = 2024;
        CompletedCourse course = CompletedCourseFixture.createCompletedCourse(CategoryType.MAJOR_BASIC);
        CategoryType criterionCategoryType = CategoryType.ACADEMIC_BASIC;

        // when
        boolean result = majorBasicPolicy.matchesCriterionCategory(admissionYear, course, criterionCategoryType);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("23학번 이하에서 전기는 기필로 보정한다.")
    void matchesCriterionCategory_returnTrue_whenLegacyJeongi() {
        // given
        int admissionYear = 2023;
        CompletedCourse course = CompletedCourseFixture.createCompletedCourse(CategoryType.MAJOR_BASIC);
        CategoryType criterionCategoryType = CategoryType.ACADEMIC_BASIC;

        // when
        boolean result = majorBasicPolicy.matchesCriterionCategory(admissionYear, course, criterionCategoryType);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("2023학번 이하라도 과목의 이수구분이 전기가 아니면 기필로 보정하지 않는다.")
    void matchesCriterionCategory_returnFalse_whenNotMajorBasic() {
        // given
        int admissionYear = 2023;
        CompletedCourse course = CompletedCourseFixture.createCompletedCourse(CategoryType.MAJOR_ELECTIVE);
        CategoryType criterionCategoryType = CategoryType.ACADEMIC_BASIC;

        // when
        boolean result = majorBasicPolicy.matchesCriterionCategory(admissionYear, course, criterionCategoryType);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("기준 데이터의 이수구분이 기필이 아니면 전기로 보정하지 않는다.")
    void matchesCriterionCategory_returnFalse_whenCriterionNotAcademicBasic() {
        // given
        int admissionYear = 2023;
        CompletedCourse course = CompletedCourseFixture.createCompletedCourse(CategoryType.MAJOR_BASIC);
        CategoryType criterionCategoryType = CategoryType.MAJOR_REQUIRED;

        // when
        boolean result = majorBasicPolicy.matchesCriterionCategory(admissionYear, course, criterionCategoryType);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("23학번 이하에서 전기 과목은 기필로 변환된다.")
    void normalizeForAcademicBasic_convertToAcademicBasic_whenMajorBasic() {
        // given
        int admissionYear = 2023;
        CompletedCourse course = CompletedCourseFixture.createCompletedCourse(CategoryType.MAJOR_BASIC);
        CategoryType criterionCategoryType = CategoryType.ACADEMIC_BASIC;

        // when
        CompletedCourse normalized =
            majorBasicPolicy.normalizeForAcademicBasic(admissionYear, course, criterionCategoryType);

        // then
        assertThat(normalized.getCategoryType()).isEqualTo(CategoryType.ACADEMIC_BASIC);
        assertThat(normalized.getCuriNo()).isEqualTo(course.getCuriNo());
        assertThat(normalized.getCuriNm()).isEqualTo(course.getCuriNm());
        assertThat(normalized.getCredits()).isEqualTo(course.getCredits());
        assertThat(normalized.getGrade()).isEqualTo(course.getGrade());
        assertThat(normalized.getMajorScope()).isEqualTo(course.getMajorScope());
        assertThat(normalized.getSelectedArea()).isEqualTo(course.getSelectedArea());
    }

    @Test
    @DisplayName("24학번 이상이면 정규화 시 원본 객체를 그대로 반환한다.")
    void normalizeForAcademicBasic_returnOriginal_whenNotMajorBasic() {
        // given
        int admissionYear = 2024;
        CompletedCourse course = CompletedCourseFixture.createCompletedCourse(CategoryType.MAJOR_BASIC);
        CategoryType criterionCategoryType = CategoryType.MAJOR_REQUIRED;

        // when
        CompletedCourse normalizedCompletedCourse =
            majorBasicPolicy.normalizeForAcademicBasic(admissionYear, course, criterionCategoryType);

        // then
        assertThat(normalizedCompletedCourse).isSameAs(course);
    }
}
