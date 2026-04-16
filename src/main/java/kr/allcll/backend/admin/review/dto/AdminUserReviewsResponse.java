package kr.allcll.backend.admin.review.dto;

import java.util.List;
import kr.allcll.backend.domain.review.UserReview;

public record AdminUserReviewsResponse(
    List<AdminUserReviewResponse> reviews
) {

    public static AdminUserReviewsResponse from(List<UserReview> reviews) {
        return new AdminUserReviewsResponse(
            reviews.stream()
                .map(AdminUserReviewResponse::from)
                .toList()
        );
    }
}
