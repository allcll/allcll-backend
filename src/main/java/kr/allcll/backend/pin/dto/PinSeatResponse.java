package kr.allcll.backend.pin.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.seat.dto.SeatDto;

public record PinSeatResponse(
    Long subjectId,
    Integer seatCount,
    LocalDateTime queryTime
) {

    public static PinSeatResponse from(SeatDto seatDto) {
        return new PinSeatResponse(
            seatDto.getSubject().getId(),
            seatDto.getSeatCount(),
            seatDto.getQueryTime()
        );
    }
}
