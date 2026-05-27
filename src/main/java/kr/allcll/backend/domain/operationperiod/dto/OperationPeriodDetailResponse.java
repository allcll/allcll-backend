package kr.allcll.backend.domain.operationperiod.dto;


import java.time.LocalDateTime;
import kr.allcll.backend.domain.operationperiod.OperationPeriod;
import kr.allcll.backend.domain.operationperiod.OperationType;

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
