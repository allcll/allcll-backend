package kr.allcll.backend.domain.period.dto;


import java.time.LocalDateTime;
import kr.allcll.backend.domain.period.OperationPeriod;
import kr.allcll.backend.domain.period.OperationType;

public record OperationPeriodDetailResponse(
    OperationType operationType,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String operationPeriodMessage
) {

    public static OperationPeriodDetailResponse from(OperationPeriod operationPeriod) {
        return new OperationPeriodDetailResponse(operationPeriod.getOperationType(), operationPeriod.getStartDate(),
            operationPeriod.getEndDate(), operationPeriod.getMessage());
    }
}
