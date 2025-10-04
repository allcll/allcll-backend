package kr.allcll.backend.domain.seat.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.admin.seat.ChangeStatus;

public record SeatResponse(
    Long subjectId,
    Integer seatCount,
    LocalDateTime queryTime,
    ChangeStatus changeStatus
) {

    public static SeatResponse from(SeatDto seatDto) {
        return new SeatResponse(
            seatDto.getSubject().getId(),
            seatDto.getSeatCount(),
            seatDto.getQueryTime(),
            seatDto.getChangeStatus()
        );
    }
}
