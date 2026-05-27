package kr.allcll.backend.admin.operationperiod.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.operationperiod.OperationPeriod;
import kr.allcll.backend.domain.operationperiod.OperationType;
import kr.allcll.backend.support.semester.Semester;

public record OperationPeriodRequest(
    OperationType operationType,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String message
) {

    public OperationPeriod toPeriod(Semester semester) {
        return OperationPeriod.create(semester, operationType, startDate, endDate, message);
    }
}
