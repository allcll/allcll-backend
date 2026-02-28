package kr.allcll.backend.domain.review.dto;

public record UserReviewRequest(
    Short rate,
    String detail,
    String operationType
) {

}
