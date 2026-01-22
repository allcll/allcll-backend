package kr.allcll.backend.admin.period.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.period.Period;
import kr.allcll.backend.domain.period.ServiceType;
import kr.allcll.backend.support.semester.Semester;

public record PeriodRequest(
        ServiceType serviceType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String message
) {

    public Period toPeriod(Semester semester) {
        return Period.create(semester, serviceType, startDate, endDate, message);
    }
}
