package kr.allcll.backend.domain.review.dto;

import kr.allcll.backend.domain.operationPeriod.OperationType;

public record UserReviewRequest(
    Short rate,
    String detail,
    OperationType operationType
) {

}
