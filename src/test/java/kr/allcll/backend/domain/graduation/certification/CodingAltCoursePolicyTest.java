package kr.allcll.backend.domain.graduation.certification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.fixture.CodingCertCriterionFixture;
import kr.allcll.backend.fixture.CompletedCourseFixture;
import kr.allcll.backend.fixture.GraduationCheckCertResultFixture;
import kr.allcll.backend.fixture.GraduationDepartmentInfoFixture;
import kr.allcll.backend.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CodingAltCoursePolicyTest {

    private static final int ADMISSION_YEAR = 2025;

    @Autowired
    private CodingAltCoursePolicy codingAltCoursePolicy;

    @MockitoBean
    private CodingCertCriterionRepository codingCertCriterionRepository;

    @Test
    @DisplayName("이미 코딩 인증이 통과된 경우 대체 과목 검사를 수행하지 않는다.")
    void isSatisfied_ByAltCourse_alreadyPassed_noUpdate() {
        // given
        GraduationDepartmentInfo deptInfo =
            GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, CodingTargetType.NON_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            true,
            false
        );

        CodingCertCriterion criterion = CodingCertCriterionFixture.createNonMajorCodingCertCriterion(ADMISSION_YEAR);
        given(codingCertCriterionRepository.findCodingCertCriterion(ADMISSION_YEAR, CodingTargetType.NON_MAJOR))
            .willReturn(Optional.of(criterion));

        List<CompletedCourseDto> completedCourses = List.of(
            CompletedCourseFixture.createCompletedCourse(criterion.getAlt1CuriNo(), "A0")
        );

        // when
        boolean result = codingAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, completedCourses, certResult);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("코딩 면제 대상인 경우 대체 과목 검사를 수행하지 않는다.")
    void isSatisfied_ByAltCourse_exempt_noUpdate() {
        // given
        GraduationDepartmentInfo deptInfo =
            GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, CodingTargetType.EXEMPT);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            false,
            false
        );


        CodingCertCriterion criterion = CodingCertCriterionFixture.createNonMajorCodingCertCriterion(ADMISSION_YEAR);
        given(codingCertCriterionRepository.findCodingCertCriterion(ADMISSION_YEAR, CodingTargetType.EXEMPT))
            .willReturn(Optional.of(criterion));

        List<CompletedCourseDto> completedCourses = List.of(
            CompletedCourseFixture.createCompletedCourse(criterion.getAlt1CuriNo(), "A0")
        );

        // when
        boolean result = codingAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, completedCourses, certResult);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("코딩 전공자인 경우 대체과목이 최소 기준 학점(B0) 이상이면 코딩 인증을 통과 처리한다.")
    void isSatisfied_ByAltCourse_codingMajor_alt1AtLeastB0_pass() {
        // given
        GraduationDepartmentInfo deptInfo
            = GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, CodingTargetType.CODING_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            false,
            false
        );

        CodingCertCriterion criterion = CodingCertCriterionFixture.createMajorCodingCertCriterion(ADMISSION_YEAR);
        given(codingCertCriterionRepository.findCodingCertCriterion(ADMISSION_YEAR, CodingTargetType.CODING_MAJOR))
            .willReturn(Optional.of(criterion));

        List<CompletedCourseDto> completedCourses = List.of(
            CompletedCourseFixture.createCompletedCourse(criterion.getAlt1CuriNo(), "B0")
        );

        // when
        boolean result = codingAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, completedCourses, certResult);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("코딩 전공자인 겅우 대체과목이 최소 기준 학점(B0)미만이면 통과 처리하지 않는다.")
    void isSatisfied_ByAltCourse_codingMajor_alt1BelowB0_notPass() {
        // given
        GraduationDepartmentInfo deptInfo
            = GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, CodingTargetType.CODING_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            false,
            false
        );

        CodingCertCriterion criterion = CodingCertCriterionFixture.createMajorCodingCertCriterion(ADMISSION_YEAR);
        given(codingCertCriterionRepository.findCodingCertCriterion(ADMISSION_YEAR, CodingTargetType.CODING_MAJOR))
            .willReturn(Optional.of(criterion));

        List<CompletedCourseDto> completedCourses = List.of(
            CompletedCourseFixture.createCompletedCourse(criterion.getAlt1CuriNo(), "C0")
        );

        // when
        boolean result = codingAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, completedCourses, certResult);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("코딩 비전공자인 경우 대체과목1이 최소 기준 학점(B0) 이상이면 통과 처리한다.")
    void isSatisfied_ByAltCourse_nonMajor_alt1AtLeastB0_pass() {
        // given
        GraduationDepartmentInfo deptInfo
            = GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, CodingTargetType.NON_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            false,
            false
        );

        CodingCertCriterion criterion = CodingCertCriterionFixture.createNonMajorCodingCertCriterion(ADMISSION_YEAR);
        given(codingCertCriterionRepository.findCodingCertCriterion(ADMISSION_YEAR, CodingTargetType.NON_MAJOR))
            .willReturn(Optional.of(criterion));

        List<CompletedCourseDto> completedCourses = List.of(
            CompletedCourseFixture.createCompletedCourse(criterion.getAlt1CuriNo(), "A0")
        );

        // when
        boolean result = codingAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, completedCourses, certResult);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("코딩 비전공자인 경우 대체과목2가 P이면 통과 처리한다.")
    void isSatisfied_ByAltCourse_nonMajor_alt2Pass_pass() {
        // given
        GraduationDepartmentInfo deptInfo
            = GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, CodingTargetType.NON_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            false,
            false
        );

        CodingCertCriterion criterion = CodingCertCriterionFixture.createNonMajorCodingCertCriterion(ADMISSION_YEAR);
        given(codingCertCriterionRepository.findCodingCertCriterion(ADMISSION_YEAR, CodingTargetType.NON_MAJOR))
            .willReturn(Optional.of(criterion));

        List<CompletedCourseDto> completedCourses = List.of(
            CompletedCourseFixture.createCompletedCourse(criterion.getAlt2CuriNo(), "P")
        );

        // when
        boolean result = codingAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, completedCourses, certResult);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("코딩 비전공자인 경우 대체과목2가 NP이면 통과 처리하지 않는다.")
    void isSatisfied_ByAltCourse_nonMajor_alt2NonPass_notPass() {
        // given
        GraduationDepartmentInfo deptInfo
            = GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, CodingTargetType.NON_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            false,
            false
        );

        CodingCertCriterion criterion = CodingCertCriterionFixture.createNonMajorCodingCertCriterion(ADMISSION_YEAR);
        given(codingCertCriterionRepository.findCodingCertCriterion(ADMISSION_YEAR, CodingTargetType.NON_MAJOR))
            .willReturn(Optional.of(criterion));

        List<CompletedCourseDto> completedCourses = List.of(
            CompletedCourseFixture.createCompletedCourse(criterion.getAlt2CuriNo(), "NP")
        );

        // when
        boolean result = codingAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, completedCourses, certResult);

        // then
        assertThat(result).isFalse();
    }
}
