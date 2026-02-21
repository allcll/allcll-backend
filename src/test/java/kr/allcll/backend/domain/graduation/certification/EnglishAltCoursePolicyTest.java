package kr.allcll.backend.domain.graduation.certification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.fixture.CompletedCourseFixture;
import kr.allcll.backend.fixture.EnglishCertCriterionFixture;
import kr.allcll.backend.fixture.GraduationCheckCertResultFixture;
import kr.allcll.backend.fixture.GraduationDepartmentInfoFixture;
import kr.allcll.backend.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class EnglishAltCoursePolicyTest {

    private static final int ADMISSION_YEAR = 2025;

    @Autowired
    private EnglishAltCoursePolicy englishAltCoursePolicy;

    @MockitoBean
    private EnglishCertCriterionRepository englishCertCriterionRepository;

    @Test
    @DisplayName("이미 영어 인증이 통과된 경우 대체 과목 검사를 수행하지 않고 통과 처리하지 않는다.")
    void isSatisfied_ByAltCourse_alreadyPassed() {
        // given
        GraduationDepartmentInfo deptInfo =
            GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, EnglishTargetType.NON_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            true,
            false,
            false
        );

        // when
        boolean result = englishAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, List.of(), certResult);

        // then
        assertThat(result).isFalse();
        assertThat(certResult.getIsEnglishCertPassed()).isTrue();
    }

    @Test
    @DisplayName("영어 대체과목 기준 정보가 없을 경우 통과 처리하지 않는다.")
    void isSatisfied_ByAltCourse_noCriterion() {
        // given
        GraduationDepartmentInfo deptInfo =
            GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, EnglishTargetType.NON_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            false,
            false
        );

        given(englishCertCriterionRepository.findEnglishCertCriterionForTarget(ADMISSION_YEAR, EnglishTargetType.NON_MAJOR))
            .willReturn(Optional.empty());

        // when
        boolean result = englishAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, List.of(), certResult);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("영어 비전공자인 경우 대체과목을 이수했다면 통과 처리한다.")
    void isSatisfied_ByAltCourse_altCourseCreditEarned() {
        // given
        GraduationDepartmentInfo deptInfo =
            GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, EnglishTargetType.NON_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            false,
            false
        );

        EnglishCertCriterion criterion = EnglishCertCriterionFixture.createNonMajorEnglishCertCriterion(ADMISSION_YEAR);
        given(englishCertCriterionRepository.findEnglishCertCriterionForTarget(ADMISSION_YEAR, EnglishTargetType.NON_MAJOR))
            .willReturn(Optional.of(criterion));

        List<CompletedCourseDto> completedCourses = List.of(
            CompletedCourseFixture.createCompletedCourse(criterion.getAltCuriNo(), "A0")
        );

        // when
        boolean result = englishAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, completedCourses, certResult);

        // then
        assertThat(result).isTrue();
        assertThat(certResult.getIsEnglishCertPassed()).isFalse(); // 정책은 판단만(상태 변경 없음)
    }

    @Test
    @DisplayName("대체과목 학수번호가 일치하지 않으면 통과 처리하지 않는다.")
    void isSatisfied_ByAltCourse_altCourseNotFound() {
        // given
        GraduationDepartmentInfo deptInfo =
            GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, EnglishTargetType.NON_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            false,
            false
        );

        EnglishCertCriterion criterion = EnglishCertCriterionFixture.createNonMajorEnglishCertCriterion(ADMISSION_YEAR);
        given(englishCertCriterionRepository.findEnglishCertCriterionForTarget(ADMISSION_YEAR, EnglishTargetType.NON_MAJOR))
            .willReturn(Optional.of(criterion));

        List<CompletedCourseDto> completedCourses = List.of(
            CompletedCourseFixture.createCompletedCourse("999999", "A0")
        );

        // when
        boolean result = englishAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, completedCourses, certResult);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("대체과목을 수강했더라도 학점 미인정 기준에 해당되면 통과 처리하지 않는다.")
    void isSatisfied_ByAltCourse_notCreditEarned() {
        // given
        GraduationDepartmentInfo deptInfo =
            GraduationDepartmentInfoFixture.createDepartmentInfo(ADMISSION_YEAR, EnglishTargetType.NON_MAJOR);
        User user = UserFixture.singleMajorUser(ADMISSION_YEAR, deptInfo);

        GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
            user,
            GraduationCertRuleType.TWO_OF_THREE,
            false,
            false,
            false
        );

        EnglishCertCriterion criterion = EnglishCertCriterionFixture.createNonMajorEnglishCertCriterion(ADMISSION_YEAR);
        given(englishCertCriterionRepository.findEnglishCertCriterionForTarget(ADMISSION_YEAR, EnglishTargetType.NON_MAJOR))
            .willReturn(Optional.of(criterion));

        List<CompletedCourseDto> completedCourses = List.of(
            CompletedCourseFixture.createCompletedCourse(criterion.getAltCuriNo(), "NP")
        );

        // when
        boolean result = englishAltCoursePolicy.isSatisfiedByAltCourse(user, deptInfo, completedCourses, certResult);

        // then
        assertThat(result).isFalse();
    }
}
