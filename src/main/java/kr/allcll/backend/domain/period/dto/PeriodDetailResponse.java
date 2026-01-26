package kr.allcll.backend.domain.period.dto;


import java.time.LocalDateTime;
import kr.allcll.backend.domain.period.OperationType;

public record PeriodDetailResponse(
    OperationType operationType,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String message
) {

}
