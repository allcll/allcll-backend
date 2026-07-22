package kr.allcll.backend.domain.graduation.credit;

import static kr.allcll.backend.fixture.CompletedCourseFixture.createCompletedCourse;
import static kr.allcll.backend.fixture.GraduationDepartmentInfoFixture.createDepartmentInfo;
import static kr.allcll.backend.fixture.UserFixture.singleMajorUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredResolver;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@Import({
    GeneralElectiveRequiredCourseChecker.class,
    NonMajorCategoryResolver.class,
    RequiredCourseResolver.class,
    UncompletedCourseFilter.class
})
class GeneralElectiveRequiredCourseCheckerIntegrationTest {

    private static final int ADMISSION_YEAR = 2021;
    private static final String REQUIRED_CURI_NO = "000001";
    private static final String REQUIRED_CURI_NM = "세계사:인간과문명";

    @Autowired
    private GeneralElectiveRequiredCourseChecker checker;

    @Autowired
    private CreditCriterionRepository creditCriterionRepository;

    @Autowired
    private RequiredCourseRepository requiredCourseRepository;

    @Autowired
    private CourseEquivalenceRepository courseEquivalenceRepository;

    @Autowired
    private GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;

    @MockitoBean
    private BalanceRequiredResolver balanceRequiredResolver;

    @AfterEach
    void clean() {
        courseEquivalenceRepository.deleteAllInBatch();
        requiredCourseRepository.deleteAllInBatch();
        creditCriterionRepository.deleteAllInBatch();
        graduationDepartmentInfoRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("교선 지정 과목을 이수하지 않으면 졸업요건을 충족하지 않는다")
    void returnsFalseWhenGeneralElectiveRequiredCourseIsUncompleted() {
        // given
        User user = saveUserAndGeneralElectiveCriterion();
        saveRequiredCourse("ALL", "0", true);

        // when
        boolean result = checker.isSatisfied(user, List.of());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("교선 지정 과목 또는 동일과목을 이수하면 졸업요건을 충족한다")
    void returnsTrueWhenGeneralElectiveRequiredCourseOrEquivalentIsCompleted() {
        // given
        User user = saveUserAndGeneralElectiveCriterion();
        saveRequiredCourse("ALL", "0", true);
        courseEquivalenceRepository.save(new CourseEquivalence("9717", REQUIRED_CURI_NO, REQUIRED_CURI_NM));
        courseEquivalenceRepository.save(new CourseEquivalence("9717", "000002", "구 세계사"));

        // when
        boolean result = checker.isSatisfied(
            user,
            List.of(createCompletedCourse("000002", "구 세계사", CategoryType.GENERAL_ELECTIVE))
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("학과별 지정 해제는 전체 교선 지정 과목보다 우선한다")
    void appliesDepartmentSpecificRequiredCourseOverride() {
        // given
        User user = saveUserAndGeneralElectiveCriterion();
        saveRequiredCourse("ALL", "0", true);
        saveRequiredCourse(user.getDeptNm(), user.getDeptCd(), false);

        // when
        boolean result = checker.isSatisfied(user, List.of());

        // then
        assertThat(result).isTrue();
    }

    private User saveUserAndGeneralElectiveCriterion() {
        GraduationDepartmentInfo department = createDepartmentInfo(ADMISSION_YEAR, CodingTargetType.NON_MAJOR);
        graduationDepartmentInfoRepository.save(department);
        given(balanceRequiredResolver.resolve(ADMISSION_YEAR, department.getDeptCd(), department.getDeptGroup()))
            .willReturn(Optional.empty());

        creditCriterionRepository.save(new CreditCriterion(
            ADMISSION_YEAR,
            ADMISSION_YEAR % 100,
            MajorType.ALL,
            department.getDeptCd(),
            department.getDeptNm(),
            MajorScope.PRIMARY,
            CategoryType.GENERAL_ELECTIVE,
            21,
            true,
            null
        ));
        return singleMajorUser(ADMISSION_YEAR, department);
    }

    private void saveRequiredCourse(String deptNm, String deptCd, boolean required) {
        requiredCourseRepository.save(new RequiredCourse(
            ADMISSION_YEAR,
            ADMISSION_YEAR % 100,
            deptCd,
            deptNm,
            CategoryType.GENERAL_ELECTIVE,
            REQUIRED_CURI_NO,
            REQUIRED_CURI_NM,
            null,
            "9717",
            required,
            null
        ));
    }
}
