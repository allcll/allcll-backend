package kr.allcll.backend.admin.review.dto;

import kr.allcll.backend.domain.operationPeriod.OperationType;
import kr.allcll.backend.domain.review.UserReview;

public record AdminUserReviewResponse(
    String studentId,
    Short rate,
    String detail,
    OperationType operationType
) {

    public static AdminUserReviewResponse from(UserReview userReview) {
        return new AdminUserReviewResponse(
            userReview.getStudentId(),
            userReview.getRate(),
            userReview.getDetail(),
            userReview.getOperationType()
        );
    }
}
