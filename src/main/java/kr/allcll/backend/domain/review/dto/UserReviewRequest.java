package kr.allcll.backend.domain.review.dto;

import jakarta.validation.constraints.Size;
import kr.allcll.backend.domain.operationperiod.OperationType;

public record UserReviewRequest(
    Short rate,

    @Size(max = 1000)
    String detail,

    OperationType operationType
) {

}
