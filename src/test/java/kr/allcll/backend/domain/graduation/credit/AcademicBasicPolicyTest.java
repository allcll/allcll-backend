package kr.allcll.backend.domain.graduation.credit;

import static kr.allcll.backend.fixture.CompletedCourseFixture.createCompletedCourse;
import static kr.allcll.backend.fixture.CourseEquivalenceFixture.createCourseEquivalence;
import static kr.allcll.backend.fixture.CreditCriterionFixture.createAcademicBasicCriterion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Set;
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
    void passThroughCourseWhenNotAcademicBasic() {
        // given
        CompletedCourse course = createCompletedCourse("123456", "알고리즘및실습", CategoryType.MAJOR_BASIC);
        CreditCriterion criterion = createAcademicBasicCriterion("컴퓨터공학과", 2021);

        // when
        List<CompletedCourse> result = academicBasicPolicy.filterRecentMajorAcademicBasic(List.of(course), criterion);

        // then
        assertThat(result).containsExactly(course);
    }

    @Test
    @DisplayName("학과 지정과목명에 있는 과목은 학문기초로 인정한다")
    void acceptCourseWhenNameMatchesRequiredCourse() {
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
        List<CompletedCourse> result = academicBasicPolicy.filterRecentMajorAcademicBasic(List.of(course), criterion);

        // then
        assertThat(result).containsExactly(course);
    }

    @Test
    @DisplayName("지정과목명에 없어도 동일과목 그룹으로 인정되면 학문기초로 인정한다")
    void acceptCourseWhenGroupCodeMatchesRequiredCourse() {
        // given
        String curiNo = "123456";
        String sameCourseCode = "1";
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
        given(courseEquivalenceRepository.findAllByCuriNoIn(Set.of(curiNo)))
            .willReturn(List.of(createCourseEquivalence(curiNo, sameCourseCode)));
        given(
            requiredCourseResolver.findRequiredCourseInGroups(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC,
                Set.of(sameCourseCode)
            )
        ).willReturn(Set.of(sameCourseCode));

        // when
        List<CompletedCourse> result = academicBasicPolicy.filterRecentMajorAcademicBasic(List.of(course), criterion);

        // then
        assertThat(result).containsExactly(course);
    }

    @Test
    @DisplayName("여러 동일과목 그룹에 속하면 그중 하나만 인정되어도 학문기초로 인정한다")
    void acceptCourseWhenAnyOfMultipleGroupsMatchesRequiredCourse() {
        // given
        String curiNo = "009960";
        String firstGroupCode = "1";
        String secondGroupCode = "2";
        String departmentName = "컴퓨터공학과";
        String curiNm = "Capstone디자인(산학협력프로젝트)";
        Integer admissionYear = 2021;
        CompletedCourse course = createCompletedCourse(curiNo, curiNm, CategoryType.ACADEMIC_BASIC);
        CreditCriterion criterion = createAcademicBasicCriterion(departmentName, admissionYear);

        given(
            requiredCourseResolver.findRequiredCourseNames(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC
            )
        ).willReturn(List.of());
        given(courseEquivalenceRepository.findAllByCuriNoIn(Set.of(curiNo)))
            .willReturn(List.of(
                createCourseEquivalence(curiNo, firstGroupCode),
                createCourseEquivalence(curiNo, secondGroupCode)
            ));
        given(
            requiredCourseResolver.findRequiredCourseInGroups(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC,
                Set.of(firstGroupCode, secondGroupCode)
            )
        ).willReturn(Set.of(secondGroupCode));

        // when
        List<CompletedCourse> result = academicBasicPolicy.filterRecentMajorAcademicBasic(List.of(course), criterion);

        // then
        assertThat(result).containsExactly(course);
    }

    @Test
    @DisplayName("동일과목 그룹에 속해도 해당 학과의 지정과목이 아니면 학문기초로 인정하지 않는다")
    void rejectCourseWhenGroupCodeNotMatchesRequiredCourse() {
        // given
        String curiNo = "123456";
        String sameCourseCode = "1";
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
        given(courseEquivalenceRepository.findAllByCuriNoIn(Set.of(curiNo)))
            .willReturn(List.of(createCourseEquivalence(curiNo, sameCourseCode)));
        given(
            requiredCourseResolver.findRequiredCourseInGroups(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC,
                Set.of(sameCourseCode)
            )
        ).willReturn(Set.of()); //해당 학과의 지정 과목이 required=false인 경우

        // when
        List<CompletedCourse> result = academicBasicPolicy.filterRecentMajorAcademicBasic(List.of(course), criterion);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("지정과목명에도 없고 동일과목 그룹에도 속하지 않으면 학문기초로 인정하지 않는다")
    void rejectCourseWhenCuriNoNotInEquivalence() {
        // given
        String curiNo = "123456";
        String departmentName = "컴퓨터공학과";
        Integer admissionYear = 2021;
        CompletedCourse course = createCompletedCourse(curiNo, "존재하지않는과목", CategoryType.ACADEMIC_BASIC);
        CreditCriterion criterion = createAcademicBasicCriterion(departmentName, admissionYear);

        given(requiredCourseResolver.findRequiredCourseNames(departmentName, admissionYear, CategoryType.ACADEMIC_BASIC))
            .willReturn(List.of());
        given(courseEquivalenceRepository.findAllByCuriNoIn(Set.of(curiNo)))
            .willReturn(List.of());

        // when
        List<CompletedCourse> result = academicBasicPolicy.filterRecentMajorAcademicBasic(List.of(course), criterion);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("학문기초 과목이 여러 개여도 지정과목·동일과목 조회는 각각 한 번만 수행한다")
    void queryOncePerCourseList() {
        // given
        String departmentName = "컴퓨터공학과";
        Integer admissionYear = 2021;
        List<CompletedCourse> courses = List.of(
            createCompletedCourse("100001", "공학설계기초", CategoryType.ACADEMIC_BASIC),
            createCompletedCourse("100002", "옛날공학설계기초", CategoryType.ACADEMIC_BASIC),
            createCompletedCourse("100003", "존재하지않는과목", CategoryType.ACADEMIC_BASIC)
        );
        CreditCriterion criterion = createAcademicBasicCriterion(departmentName, admissionYear);

        given(requiredCourseResolver.findRequiredCourseNames(departmentName, admissionYear, CategoryType.ACADEMIC_BASIC))
            .willReturn(List.of("공학설계기초"));
        given(courseEquivalenceRepository.findAllByCuriNoIn(Set.of("100002", "100003")))
            .willReturn(List.of(createCourseEquivalence("100002", "1")));
        given(
            requiredCourseResolver.findRequiredCourseInGroups(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC,
                Set.of("1")
            )
        ).willReturn(Set.of("1"));

        // when
        List<CompletedCourse> result = academicBasicPolicy.filterRecentMajorAcademicBasic(courses, criterion);

        // then
        assertThat(result).containsExactly(courses.get(0), courses.get(1));
        then(requiredCourseResolver).should(times(1))
            .findRequiredCourseNames(anyString(), anyInt(), any(CategoryType.class));
        then(courseEquivalenceRepository).should(times(1)).findAllByCuriNoIn(any());
    }
}
