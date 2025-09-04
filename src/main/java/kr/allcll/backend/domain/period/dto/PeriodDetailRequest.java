package kr.allcll.backend.domain.period.dto;


import java.time.LocalDateTime;
import kr.allcll.backend.domain.period.Period;
import kr.allcll.backend.domain.period.ServiceType;

public record PeriodDetailRequest(
    ServiceType serviceType,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String message
) {

    public static PeriodDetailRequest from(Period period) {
        return new PeriodDetailRequest(
            period.getServiceType(),
            period.getStartDate(),
            period.getEndDate(),
            period.getMessage()
        );
    }

}
