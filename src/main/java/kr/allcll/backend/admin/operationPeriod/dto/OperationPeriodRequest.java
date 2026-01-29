package kr.allcll.backend.admin.operationPeriod.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.operationPeriod.OperationPeriod;
import kr.allcll.backend.domain.operationPeriod.OperationType;
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
