package kr.allcll.backend.domain.period.dto;


import java.time.LocalDateTime;
import kr.allcll.backend.domain.period.ServiceType;

public record PeriodDetailResponse(
    ServiceType serviceType,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String message
) {

}
