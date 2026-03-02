package kr.allcll.backend.domain.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRule;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleRepository;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.operationPeriod.OperationType;
import kr.allcll.backend.domain.review.dto.UserReviewRequest;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.fixture.UserFixture;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserReviewServiceTest {

    @Autowired
    private UserReviewService userReviewService;

    @Autowired
    private GraduationCertRuleRepository certRuleRepository;

    @Autowired
    private GraduationDepartmentInfoRepository departmentInfoRepository;

    @MockitoBean
    private UserReviewRepository userReviewRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("유저가 없으면 USER_NOT_FOUND 예외를 던진다")
    void throw_exception_when_user_not_found() {
        // given
        Long userId = 999L;
        UserReviewRequest request = new UserReviewRequest(
            (short) 2,
            "좋네요",
            OperationType.LIVE
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userReviewService.createReview(userId, request))
            .isInstanceOfSatisfying(AllcllException.class, exception -> {
                assertThat(exception.getErrorCode()).isEqualTo(AllcllErrorCode.USER_NOT_FOUND);
            });

        verify(userReviewRepository, never()).save(any(UserReview.class));
    }

    @Test
    @DisplayName("유저가 존재하면 리뷰를 생성해 저장한다")
    void saveReviewWithCorrectFields() {
        // given
        Long userId = 1L;
        int admissionYear = 2025;
        String reviewDetail = "올클 화이팅";

        certRuleRepository.saveAndFlush(new GraduationCertRule(admissionYear, 25, GraduationCertRuleType.TWO_OF_THREE));
        GraduationDepartmentInfo deptInfo = departmentInfoRepository.saveAndFlush(
            new GraduationDepartmentInfo(
                admissionYear,
                25,
                "수학통계학과",
                "2658",
                "자연과학대학",
                DeptGroup.NATURAL_SCIENCES_COLLEGE,
                EnglishTargetType.NON_MAJOR,
                CodingTargetType.NON_MAJOR,
                null
            )
        );

        User user = UserFixture.singleMajorUser(admissionYear, deptInfo);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        UserReviewRequest request = new UserReviewRequest(
            (short) 4,
            reviewDetail,
            OperationType.PRESEAT
        );

        given(userReviewRepository.save(any(UserReview.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // when
        userReviewService.createReview(userId, request);

        // then
        var userReview = org.mockito.ArgumentCaptor.forClass(UserReview.class);
        verify(userReviewRepository).save(userReview.capture());
        UserReview saved = userReview.getValue();

        assertThat(saved.getStudentId()).isEqualTo(user.getStudentId());
        assertThat(saved.getRate()).isEqualTo((short) 4);
        assertThat(saved.getOperationType()).isEqualTo(OperationType.PRESEAT);
        assertThat(saved.getDetail()).isEqualTo(reviewDetail);
    }
}
