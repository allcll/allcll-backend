package kr.allcll.backend.domain.graduation.credit;

import static kr.allcll.backend.fixture.CompletedCourseFixture.createCompletedCourse;
import static kr.allcll.backend.fixture.CreditCriterionFixture.createAcademicBasicCriterion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AcademicBasicPolicyTest {

    @Autowired
    private AcademicBasicPolicy academicBasicPolicy;

    @MockitoBean
    private RequiredCourseResolver requiredCourseResolver;

    @MockitoBean
    private CourseEquivalenceRepository courseEquivalenceRepository;

    @Test
    @DisplayName("ACADEMIC_BASIC이 아닌 과목은 검사 없이 통과한다")
    void returnTrueWhenNotAcademicBasic() {
        // given
        CompletedCourse course = createCompletedCourse("123456", "알고리즘및실습", CategoryType.MAJOR_BASIC);
        CreditCriterion criterion = createAcademicBasicCriterion("컴퓨터공학과", 2021);

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("필수 과목 리스트에 존재하는 과목이면 true를 반환한다")
    void returnTrueWhenDirectMatch() {
        // given
        String courseName = "공학설계기초";
        String departmentName = "컴퓨터공학과";
        Integer admissionYear = 2021;
        CompletedCourse course = createCompletedCourse("123456", courseName, CategoryType.ACADEMIC_BASIC);
        CreditCriterion criterion = createAcademicBasicCriterion(departmentName, admissionYear);

        given(
            requiredCourseResolver.findRequiredCourseNames(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC)
        ).willReturn(List.of("공학설계기초", "기초미적분학"));

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("필수 과목에 없지만 동일과목 그룹에 속하면 true를 반환한다")
    void returnTrueWhenGroupCodeMatchesRequiredCourse() {
        // given
        String curiNo = "123456";
        String groupCode = "1";
        String departmentName = "컴퓨터공학과";
        Integer admissionYear = 2021;
        CompletedCourse course = createCompletedCourse(curiNo, "옛날공학설계기초", CategoryType.ACADEMIC_BASIC);
        CreditCriterion criterion = createAcademicBasicCriterion(departmentName, admissionYear);

        given(
            requiredCourseResolver.findRequiredCourseNames(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC
            )
        ).willReturn(List.of());
        given(courseEquivalenceRepository.findGroupCodeByCuriNo(curiNo))
            .willReturn(Optional.of(groupCode));
        given(
            requiredCourseResolver.findRequiredCourseInGroup(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC,
                groupCode
            )
        ).willReturn(true);

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("과목명이 필수과목에 없고 동일과목 그룹에 속하지만, 해당 과의 이수 요건에 해당하지 않으면 false를 반환한다")
    void returnFalseWhenGroupCodeNotMatchesRequiredCourse() {
        String curiNo = "123456";
        String groupCode = "1";
        String departmentName = "컴퓨터공학과";
        Integer admissionYear = 2021;
        CompletedCourse course = createCompletedCourse(curiNo, "옛날공학설계기초", CategoryType.ACADEMIC_BASIC);
        CreditCriterion criterion = createAcademicBasicCriterion(departmentName, admissionYear);

        given(
            requiredCourseResolver.findRequiredCourseNames(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC
            )
        ).willReturn(List.of());
        given(courseEquivalenceRepository.findGroupCodeByCuriNo(curiNo))
            .willReturn(Optional.of(groupCode));
        given(
            requiredCourseResolver.findRequiredCourseInGroup(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC,
                groupCode)
        ).willReturn(false); //해당 학과의 지정 과목이 required=false인 경우

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("과목명이 필수과목에도 없고 동일과목 그룹에도 속하지 않으면 false를 반환한다")
    void returnFalseWhenCuriNoNotInEquivalence() {
        // given
        String curiNo = "123456";
        String departmentName = "컴퓨터공학과";
        Integer admissionYear = 2021;
        CompletedCourse course = createCompletedCourse(curiNo, "존재하지않는과목", CategoryType.ACADEMIC_BASIC);
        CreditCriterion criterion = createAcademicBasicCriterion(departmentName, admissionYear);

        given(requiredCourseResolver.findRequiredCourseNames(departmentName, admissionYear, CategoryType.ACADEMIC_BASIC))
            .willReturn(List.of());
        given(courseEquivalenceRepository.findGroupCodeByCuriNo(curiNo))
            .willReturn(Optional.empty());

        // when
        boolean result = academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion);

        // then
        assertThat(result).isFalse();
    }
}
