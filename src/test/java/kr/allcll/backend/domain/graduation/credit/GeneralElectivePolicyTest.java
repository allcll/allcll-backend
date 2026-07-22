package kr.allcll.backend.domain.graduation.credit;

import static kr.allcll.backend.fixture.CompletedCourseFixture.createCompletedCourse;
import static kr.allcll.backend.fixture.GraduationDepartmentInfoFixture.createDepartmentInfo;
import static kr.allcll.backend.fixture.UserFixture.singleMajorUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import kr.allcll.backend.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeneralElectivePolicyTest {

    @Mock
    private CreditCriterionRepository creditCriterionRepository;

    @Mock
    private RequiredCourseResolver requiredCourseResolver;

    @Mock
    private CourseEquivalenceRepository courseEquivalenceRepository;

    @Test
    @DisplayName("활성화된 교선 기준의 지정 과목을 이수하지 않으면 통과하지 않는다")
    void failsWhenEnabledGeneralElectiveHasUncompletedRequiredCourse() {
        User user = user();
        when(creditCriterionRepository.findByAdmissionYearAndMajorTypeAndDeptCd(2021, MajorType.ALL, "3220"))
            .thenReturn(List.of(generalElectiveCriterion(true)));
        when(requiredCourseResolver.resolveRequiredCourses(2021, "3220"))
            .thenReturn(generalElectiveRequiredCourse());

        assertThat(policy().areRequiredCoursesSatisfied(user, List.of())).isFalse();
    }

    @Test
    @DisplayName("비활성화된 교선 기준은 지정 과목 검사를 적용하지 않는다")
    void skipsRequiredCourseValidationWhenGeneralElectiveIsDisabled() {
        User user = user();
        when(creditCriterionRepository.findByAdmissionYearAndMajorTypeAndDeptCd(2021, MajorType.ALL, "3220"))
            .thenReturn(List.of(generalElectiveCriterion(false)));

        assertThat(policy().areRequiredCoursesSatisfied(user, List.of())).isTrue();
        verify(requiredCourseResolver, never()).resolveRequiredCourses(2021, "3220");
    }

    @Test
    @DisplayName("동일·대체 과목을 이수하면 교선 지정 과목을 이수한 것으로 인정한다")
    void acceptsEquivalentCourse() {
        User user = user();
        when(creditCriterionRepository.findByAdmissionYearAndMajorTypeAndDeptCd(2021, MajorType.ALL, "3220"))
            .thenReturn(List.of(generalElectiveCriterion(true)));
        when(requiredCourseResolver.resolveRequiredCourses(2021, "3220"))
            .thenReturn(generalElectiveRequiredCourse());
        when(courseEquivalenceRepository.findSameGroupCuriNos(java.util.Set.of("ALT001")))
            .thenReturn(List.of("REQ001", "ALT001"));
        CompletedCourse equivalentCourse = createCompletedCourse("ALT001", "대체과목", CategoryType.GENERAL_ELECTIVE);

        assertThat(policy().areRequiredCoursesSatisfied(user, List.of(equivalentCourse))).isTrue();
    }

    @Test
    @DisplayName("18~20학번도 활성화된 교선 지정 과목을 이수해야 한다")
    void appliesTo2018Through2020Cohorts() {
        for (int admissionYear : List.of(2018, 2019, 2020)) {
            User user = user(admissionYear);
            when(creditCriterionRepository.findByAdmissionYearAndMajorTypeAndDeptCd(admissionYear, MajorType.ALL, "3220"))
                .thenReturn(List.of(generalElectiveCriterion(admissionYear, true)));
            when(requiredCourseResolver.resolveRequiredCourses(admissionYear, "3220"))
                .thenReturn(generalElectiveRequiredCourse());

            assertThat(policy().areRequiredCoursesSatisfied(user, List.of())).isFalse();
        }
    }

    @Test
    @DisplayName("교선 기준이 없는 2022학번 이후에는 지정 과목 검사를 적용하지 않는다")
    void skipsCohortsWithoutGeneralElectiveCriterion() {
        User user = user(2022);
        when(creditCriterionRepository.findByAdmissionYearAndMajorTypeAndDeptCd(2022, MajorType.ALL, "3220"))
            .thenReturn(List.of());

        assertThat(policy().areRequiredCoursesSatisfied(user, List.of())).isTrue();
        verify(requiredCourseResolver, never()).resolveRequiredCourses(2022, "3220");
    }

    private GeneralElectivePolicy policy() {
        return new GeneralElectivePolicy(
            creditCriterionRepository,
            requiredCourseResolver,
            new UncompletedCourseFilter(courseEquivalenceRepository)
        );
    }

    private User user() {
        return user(2021);
    }

    private User user(int admissionYear) {
        return singleMajorUser("21000000", admissionYear, createDepartmentInfo(admissionYear, CodingTargetType.NON_MAJOR));
    }

    private CreditCriterion generalElectiveCriterion(boolean enabled) {
        return generalElectiveCriterion(2021, enabled);
    }

    private Map<CategoryType, List<RequiredCourseResponse>> generalElectiveRequiredCourse() {
        return Map.of(
            CategoryType.GENERAL_ELECTIVE,
            List.of(RequiredCourseResponse.of("REQ001", "지정과목"))
        );
    }

    private CreditCriterion generalElectiveCriterion(int admissionYear, boolean enabled) {
        return new CreditCriterion(
            admissionYear,
            admissionYear % 100,
            MajorType.ALL,
            "3220",
            "테스트학과",
            MajorScope.PRIMARY,
            CategoryType.GENERAL_ELECTIVE,
            0,
            enabled,
            ""
        );
    }
}
