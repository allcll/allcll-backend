package kr.allcll.backend.domain.operationPeriod.dto;


import java.time.LocalDateTime;
import kr.allcll.backend.domain.operationPeriod.OperationPeriod;
import kr.allcll.backend.domain.operationPeriod.OperationType;

public record OperationPeriodDetailResponse(
    OperationType operationType,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String message
) {

    public static OperationPeriodDetailResponse from(OperationPeriod operationPeriod) {
        return new OperationPeriodDetailResponse(operationPeriod.getOperationType(), operationPeriod.getStartDate(),
            operationPeriod.getEndDate(), operationPeriod.getMessage());
    }
}
