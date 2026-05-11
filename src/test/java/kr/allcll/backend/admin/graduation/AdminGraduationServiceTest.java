package kr.allcll.backend.admin.graduation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.allcll.backend.admin.graduation.dto.GraduationDetailResponse;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
import kr.allcll.backend.domain.graduation.balance.dto.BalanceAreaCoursesResponse;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;
import kr.allcll.backend.domain.graduation.certification.GraduationCertCriteriaService;
import kr.allcll.backend.domain.graduation.certification.dto.ClassicCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.certification.dto.EnglishCertAltCourseResponse;
import kr.allcll.backend.domain.graduation.certification.dto.EnglishCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaTargetResponse;
import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertPolicyResponse;
import kr.allcll.backend.domain.graduation.check.result.GraduationCheckService;
import kr.allcll.backend.domain.graduation.check.result.dto.CertificationPolicy;
import kr.allcll.backend.domain.graduation.check.result.dto.ClassicCertification;
import kr.allcll.backend.domain.graduation.check.result.dto.ClassicDomainRequirement;
import kr.allcll.backend.domain.graduation.check.result.dto.CodingCertification;
import kr.allcll.backend.domain.graduation.check.result.dto.CompletedCourseResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.CompletedCoursesResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.EnglishCertification;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCategory;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCertifications;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCheckResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationSummary;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.credit.GraduationCategoryService;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoriesResponse;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoryResponse;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationContextResponse;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.fixture.UserFixture;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminGraduationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GraduationCheckService graduationCheckService;

    @Mock
    private GraduationCategoryService graduationCategoryService;

    @Mock
    private GraduationCertCriteriaService graduationCertCriteriaService;

    @InjectMocks
    private AdminGraduationService adminGraduationService;

    @Test
    @DisplayName("학번으로 졸업요건 상세 정보를 조회하면 응답이 조립된다.")
    void getGraduationDetail() {
        // given
        String studentId = "22011799";
        Long userId = 1L;
        User user = UserFixture.singleMajorUser(studentId);
        ReflectionTestUtils.setField(user, "id", userId);

        GraduationCheckResponse checkData = createCheckData(userId);
        CompletedCoursesResponse courses = createCourses();
        GraduationCategoriesResponse criteriaCategories = createCriteriaCategories();
        GraduationCertCriteriaResponse certCriteria = createCertCriteria();

        given(userRepository.findByStudentId(studentId)).willReturn(Optional.of(user));
        given(graduationCheckService.getCheckResult(userId)).willReturn(checkData);
        given(graduationCheckService.getAllCompletedCourses(userId)).willReturn(courses);
        given(graduationCategoryService.getAllCategories(userId)).willReturn(criteriaCategories);
        given(graduationCertCriteriaService.getGraduationCertCriteria(userId)).willReturn(certCriteria);

        // when
        GraduationDetailResponse response = adminGraduationService.getGraduationDetail(studentId);

        // then
        assertThat(response.user().studentId()).isEqualTo(studentId);
        assertThat(response.user().majorType()).isEqualTo("SINGLE");
        assertThat(response.checkData().isGraduatable()).isFalse();
        assertThat(response.checkData().summary().totalMyCredits()).isEqualTo(114.0);
        assertThat(response.courses().courses()).hasSize(1);
        assertThat(response.criteriaCategories().context().primaryDeptCd()).isEqualTo("3220");
        assertThat(response.certificationCriteria().classicCertCriteria().totalRequiredCount()).isEqualTo(10);

        then(graduationCheckService).should().getCheckResult(userId);
        then(graduationCheckService).should().getAllCompletedCourses(userId);
        then(graduationCategoryService).should().getAllCategories(userId);
        then(graduationCertCriteriaService).should().getGraduationCertCriteria(userId);
    }

    @Test
    @DisplayName("존재하지 않는 학번으로 조회하면 USER_NOT_FOUND 예외가 발생한다.")
    void getGraduationDetail_userNotFound() {
        // given
        String notExistStudentId = "99999999";
        given(userRepository.findByStudentId(notExistStudentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminGraduationService.getGraduationDetail(notExistStudentId))
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("졸업요건 검사 이력이 없으면 GRADUATION_CHECK_NOT_FOUND 예외가 발생한다.")
    void getGraduationDetail_graduationCheckNotFound() {
        // given
        String studentId = "22011799";
        Long userId = 1L;
        User user = UserFixture.singleMajorUser(studentId);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findByStudentId(studentId)).willReturn(Optional.of(user));
        given(graduationCheckService.getCheckResult(userId))
            .willThrow(new AllcllException(AllcllErrorCode.GRADUATION_CHECK_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> adminGraduationService.getGraduationDetail(studentId))
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.GRADUATION_CHECK_NOT_FOUND.getMessage());
    }

    private GraduationCheckResponse createCheckData(Long userId) {
        return new GraduationCheckResponse(
            userId,
            LocalDateTime.of(2026, 2, 28, 3, 41, 25),
            false,
            new GraduationSummary(114.0, 130, 16.0),
            List.of(
                new GraduationCategory(
                    MajorScope.PRIMARY,
                    CategoryType.ACADEMIC_BASIC,
                    12.0,
                    15,
                    3.0,
                    null,
                    null,
                    null,
                    false
                ),
                new GraduationCategory(
                    MajorScope.PRIMARY,
                    CategoryType.BALANCE_REQUIRED,
                    6.0,
                    6,
                    0.0,
                    1,
                    2,
                    Set.of(BalanceRequiredArea.HISTORY_THOUGHT),
                    false
                ),
                new GraduationCategory(
                    MajorScope.PRIMARY,
                    CategoryType.COMMON_REQUIRED,
                    13.0,
                    13,
                    0.0,
                    null,
                    null,
                    null,
                    true
                ),
                new GraduationCategory(
                    MajorScope.PRIMARY,
                    CategoryType.TOTAL_COMPLETION,
                    114.0,
                    130,
                    16.0,
                    null,
                    null,
                    null,
                    false
                ),
                new GraduationCategory(
                    MajorScope.PRIMARY,
                    CategoryType.MAJOR_REQUIRED,
                    30.0,
                    36,
                    6.0,
                    null,
                    null,
                    null,
                    false
                ),
                new GraduationCategory(
                    MajorScope.PRIMARY,
                    CategoryType.MAJOR_ELECTIVE,
                    30.0,
                    36,
                    6.0,
                    null,
                    null,
                    null,
                    false
                )
            ),
            new GraduationCertifications(
                new CertificationPolicy("BOTH_REQUIRED", 2),
                1,
                2,
                false,
                new EnglishCertification(true, false, EnglishTargetType.NON_MAJOR),
                new CodingCertification(false, false, CodingTargetType.EXEMPT),
                new ClassicCertification(true, true, 10, 6, List.of(
                    new ClassicDomainRequirement("WESTERN_HISTORY_THOUGHT", 4, 2, false),
                    new ClassicDomainRequirement("EASTERN_HISTORY_THOUGHT", 2, 1, false),
                    new ClassicDomainRequirement("EAST_WEST_LITERATURE", 3, 2, false),
                    new ClassicDomainRequirement("SCIENCE_THOUGHT", 1, 1, true)
                ))
            )
        );
    }

    private CompletedCoursesResponse createCourses() {
        return new CompletedCoursesResponse(
            LocalDateTime.of(2026, 5, 10, 11, 15, 37),
            List.of(
                new CompletedCourseResponse(
                    1L,
                    "003284",
                    "컴퓨터네트워크",
                    CategoryType.MAJOR_ELECTIVE,
                    "",
                    3.0,
                    MajorScope.PRIMARY,
                    true
                )
            )
        );
    }

    private GraduationCategoriesResponse createCriteriaCategories() {
        return new GraduationCategoriesResponse(
            GraduationContextResponse.of(
                2022,
                MajorType.SINGLE,
                "3220",
                "소프트웨어학과",
                null,
                null
            ),
            List.of(
                GraduationCategoryResponse.of(
                    MajorScope.PRIMARY,
                    CategoryType.COMMON_REQUIRED,
                    true,
                    13,
                    List.of()
                ),
                GraduationCategoryResponse.balanceRequiredOf(
                    CategoryType.BALANCE_REQUIRED,
                    true,
                    6,
                    2,
                    List.of(
                        BalanceAreaCoursesResponse.of(
                            BalanceRequiredArea.HISTORY_THOUGHT,
                            List.of(
                                RequiredCourseResponse.of("011306", "성서와기독교"),
                                RequiredCourseResponse.of("011307", "세계사")
                            )
                        )
                    ),
                    BalanceRequiredArea.NATURE_SCIENCE
                ),
                GraduationCategoryResponse.of(
                    MajorScope.PRIMARY,
                    CategoryType.ACADEMIC_BASIC,
                    true,
                    15,
                    List.of(RequiredCourseResponse.of("003353", "통계학개론"))
                ),
                GraduationCategoryResponse.of(
                    MajorScope.PRIMARY,
                    CategoryType.MAJOR_REQUIRED,
                    true,
                    36,
                    List.of(RequiredCourseResponse.of("009960", "Capstone디자인(산학협력프로젝트)"))
                )
            )
        );
    }

    private GraduationCertCriteriaResponse createCertCriteria() {
        return GraduationCertCriteriaResponse.of(
            GraduationCertCriteriaTargetResponse.of("NON_MAJOR", "EXEMPT"),
            GraduationCertPolicyResponse.of(
                "BOTH_REQUIRED",
                2,
                true,
                true,
                false
            ),
            EnglishCertCriteriaResponse.of(
                "NON_MAJOR",
                700,
                80,
                556,
                301,
                "Intermediate Low",
                "Intermediate Low",
                2,
                65,
                0,
                EnglishCertAltCourseResponse.of("006844", "Intensive English", 3)
            ),
            ClassicCertCriteriaResponse.fromEnum(),
            null
        );
    }
}
