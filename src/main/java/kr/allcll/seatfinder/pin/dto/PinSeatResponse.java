package kr.allcll.seatfinder.pin.dto;

import java.time.LocalDateTime;
import kr.allcll.seatfinder.seat.dto.SeatDto;

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
