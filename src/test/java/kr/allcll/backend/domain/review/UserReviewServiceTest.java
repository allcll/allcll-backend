package kr.allcll.backend.domain.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.operationPeriod.OperationType;
import kr.allcll.backend.domain.review.dto.UserReviewRequest;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.fixture.GraduationDepartmentInfoFixture;
import kr.allcll.backend.fixture.UserFixture;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserReviewServiceTest {

    @Autowired
    private UserReviewService userReviewService;

    @Autowired
    private UserReviewRepository userReviewRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void flush() {
        userRepository.flush();
        userReviewRepository.flush();
    }

    @Test
    @DisplayName("유저가 없으면 USER_NOT_FOUND 예외를 던진다")
    void throw_exception_when_user_not_found() {
        // given
        Long userId = 999L;
        UserReviewRequest request = new UserReviewRequest(
            (short) 2,
            "좋네요",
            OperationType.GRADUATION
        );

        // when & then
        assertThatThrownBy(() -> userReviewService.createReview(userId, request))
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("유저가 존재하면 리뷰를 생성해 저장한다")
    void saveReviewWithCorrectFields() {
        // given
        Long userId = 1L;
        int admissionYear = 2025;
        String reviewDetail = "올클 화이팅";
        GraduationDepartmentInfo userDept = GraduationDepartmentInfoFixture
            .createDepartmentInfo(admissionYear, CodingTargetType.CODING_MAJOR);
        User user = UserFixture.singleMajorUser(admissionYear, userDept);
        UserReviewRequest request = new UserReviewRequest(
            (short) 3,
            reviewDetail,
            OperationType.GRADUATION
        );
        userRepository.save(user);

        // when
        userReviewService.createReview(userId, request);

        // then
        List<UserReview> reviews = userReviewRepository.findReviews(user.getStudentId());
        UserReview review = reviews.getFirst();

        assertAll(
            () -> assertThat(review.getDetail()).isEqualTo(reviewDetail),
            () -> assertThat(review.getRate()).isEqualTo((short) 3),
            () -> assertThat(review.getOperationType()).isEqualTo(OperationType.GRADUATION),
            () -> assertThat(review.getStudentId()).isEqualTo(user.getStudentId())
        );
    }
}
