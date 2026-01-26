package kr.allcll.backend.admin.period.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.period.OperationPeriod;
import kr.allcll.backend.domain.period.OperationType;
import kr.allcll.backend.support.semester.Semester;

public record PeriodRequest(
        OperationType operationType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String message
) {

    public OperationPeriod toPeriod(Semester semester) {
        return OperationPeriod.create(semester, operationType, startDate, endDate, message);
    }
}
