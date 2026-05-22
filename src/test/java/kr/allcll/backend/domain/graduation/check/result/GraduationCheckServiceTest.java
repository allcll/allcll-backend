package kr.allcll.backend.domain.graduation.check.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.google.api.services.sheets.v4.Sheets;
import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.certification.CodingCertCriterionRepository;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.certification.EnglishCertCriterionRepository;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRule;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleRepository;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResultRepository;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCoursePersistenceService;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseRepository;
import kr.allcll.backend.domain.graduation.check.result.dto.CompletedCoursesResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCheckResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.UpdateEnglishCertRequest;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.fixture.CodingCertCriterionFixture;
import kr.allcll.backend.fixture.EnglishCertCriterionFixture;
import kr.allcll.backend.fixture.GraduationCheckCertResultFixture;
import kr.allcll.backend.fixture.GraduationDepartmentInfoFixture;
import kr.allcll.backend.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class GraduationCheckServiceTest {

    @Autowired
    private CompletedCoursePersistenceService completedCoursePersistenceService;

    @Autowired
    private CompletedCourseRepository completedCourseRepository;

    @Autowired
    private GraduationCheckService graduationCheckService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GraduationCheckCertResultRepository graduationCheckCertResultRepository;

    @Autowired
    private GraduationCheckRepository graduationCheckRepository;

    @Autowired
    private GraduationCheckCategoryResultRepository graduationCheckCategoryResultRepository;

    @Autowired
    private GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;

    @Autowired
    private GraduationCertRuleRepository graduationCertRuleRepository;

    @Autowired
    private EnglishCertCriterionRepository englishCertCriterionRepository;

    @Autowired
    private CodingCertCriterionRepository codingCertCriterionRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private Sheets sheets;

    @AfterEach
    void clean() {
        graduationCheckCategoryResultRepository.deleteAllInBatch();
        graduationCheckRepository.deleteAllInBatch();
        graduationCheckCertResultRepository.deleteAllInBatch();
        completedCourseRepository.deleteAllInBatch();
        codingCertCriterionRepository.deleteAllInBatch();
        englishCertCriterionRepository.deleteAllInBatch();
        graduationCertRuleRepository.deleteAllInBatch();
        graduationDepartmentInfoRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("빈환값의 개수와 내용을 확인한다.")
    void checkAllEarnedCoursesContents() {
        // given
        GraduationDepartmentInfo graduationDepartmentInfo = GraduationDepartmentInfoFixture
            .createDepartmentInfo(2021, CodingTargetType.CODING_MAJOR);
        User user = UserFixture.singleMajorUser(2021, graduationDepartmentInfo);
        userRepository.save(user);
        CompletedCourseDto completedCourseDtoA = CompletedCourseDto.of("123456",
            "과목명A",
            "기교",
            "selectedArea",
            3.0,
            "A+");

        CompletedCourseDto completedCourseDtoB = CompletedCourseDto.of("654321",
            "과목명B",
            "전필",
            "selectedArea",
            3.0,
            "FA");
        completedCoursePersistenceService.saveAllCompletedCourse(
            user.getId(),
            List.of(completedCourseDtoA, completedCourseDtoB)
        );

        // when
        CompletedCoursesResponse allEarnedCourses = graduationCheckService.getAllCompletedCourses(user.getId());

        // then
        assertThat(allEarnedCourses.courses()).hasSize(2)
            .extracting(
                "curiNo", "curiNm", "isEarned"
            )
            .containsExactlyInAnyOrder(
                tuple("123456", "과목명A", true),
                tuple("654321", "과목명B", false)
            );
    }

    @Test
    @DisplayName("영어 인증을 true로 수정하면 passedCount와 만족 여부를 재계산한다.")
    void updateEnglishCertPassTrueRecalculatesResult() {
        // given
        Long userId = saveUserWithSatisfiedGraduationCheckAndCertResult(
            GraduationCertRuleType.BOTH_REQUIRED,
            false,
            false,
            true
        );

        // when
        graduationCheckService.updateEnglishCertPassAndGetCheckResult(userId, new UpdateEnglishCertRequest(true));
        GraduationCheckCertResult updated = graduationCheckCertResultRepository.findByUserId(userId)
            .orElseThrow();

        // then
        assertThat(updated.getIsEnglishCertPassed()).isTrue();
        assertThat(updated.getPassedCount()).isEqualTo(2);
        assertThat(updated.getRequiredPassCount()).isEqualTo(2);
        assertThat(updated.getIsSatisfied()).isTrue();
    }

    @Test
    @DisplayName("영어 인증을 false로 수정하면 passedCount와 만족 여부를 재계산한다.")
    void updateEnglishCertPassFalseRecalculatesResult() {
        // given
        Long userId = saveUserWithSatisfiedGraduationCheckAndCertResult(
            GraduationCertRuleType.BOTH_REQUIRED,
            true,
            false,
            true
        );

        // when
        graduationCheckService.updateEnglishCertPassAndGetCheckResult(userId, new UpdateEnglishCertRequest(false));
        GraduationCheckCertResult updated = graduationCheckCertResultRepository.findByUserId(userId)
            .orElseThrow();

        // then
        assertThat(updated.getIsEnglishCertPassed()).isFalse();
        assertThat(updated.getPassedCount()).isEqualTo(1);
        assertThat(updated.getRequiredPassCount()).isEqualTo(2);
        assertThat(updated.getIsSatisfied()).isFalse();
    }

    @Test
    @DisplayName("영어 인증을 false로 수정하면 졸업 가능 여부도 false로 갱신된다.")
    void updateEnglishCertPassFalseUpdatesGraduatableResult() {
        // given
        Long userId = saveUserWithSatisfiedGraduationCheckAndCertResult(
            GraduationCertRuleType.BOTH_REQUIRED,
            true,
            false,
            true
        );

        // when
        GraduationCheckResponse response = graduationCheckService.updateEnglishCertPassAndGetCheckResult(
            userId,
            new UpdateEnglishCertRequest(false)
        );
        GraduationCheck updatedCheck = graduationCheckRepository.findByUserId(userId).orElseThrow();

        // then
        assertAll(
            () -> assertThat(updatedCheck.getCanGraduate()).isFalse(),
            () -> assertThat(response.isGraduatable()).isFalse()
        );
    }

    private Long saveUserWithSatisfiedGraduationCheckAndCertResult(
        GraduationCertRuleType certRuleType,
        boolean isEnglishCertPassed,
        boolean isCodingCertPassed,
        boolean isClassicsCertPassed
    ) {
        return transactionTemplate.execute(status -> {
            GraduationDepartmentInfo graduationDepartmentInfo = GraduationDepartmentInfoFixture
                .createDepartmentInfo(2021, CodingTargetType.CODING_MAJOR);
            graduationDepartmentInfoRepository.save(graduationDepartmentInfo);
            User user = userRepository.save(UserFixture.singleMajorUser(2021, graduationDepartmentInfo));
            saveSatisfiedGraduationCheck(user.getId());
            saveGraduationCriteria(2021);
            GraduationCheckCertResult certResult = GraduationCheckCertResultFixture.createCertResult(
                user,
                certRuleType,
                isEnglishCertPassed,
                isCodingCertPassed,
                isClassicsCertPassed
            );
            graduationCheckCertResultRepository.save(certResult);
            return user.getId();
        });
    }

    private void saveSatisfiedGraduationCheck(Long userId) {
        graduationCheckRepository.save(new GraduationCheck(userId, true, 130.0, 130, 0.0));
        graduationCheckCategoryResultRepository.save(new GraduationCheckCategoryResult(
            userId,
            MajorScope.PRIMARY,
            CategoryType.TOTAL_COMPLETION,
            130.0,
            130,
            0.0,
            true
        ));
    }

    private void saveGraduationCriteria(int admissionYear) {
        graduationCertRuleRepository.save(new GraduationCertRule(
            admissionYear,
            admissionYear % 100,
            GraduationCertRuleType.BOTH_REQUIRED
        ));
        englishCertCriterionRepository.save(
            EnglishCertCriterionFixture.createNonMajorEnglishCertCriterion(admissionYear)
        );
        codingCertCriterionRepository.save(
            CodingCertCriterionFixture.createMajorCodingCertCriterion(admissionYear)
        );
    }
}
