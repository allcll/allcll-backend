package kr.allcll.seatfinder.seat.dto;

import java.time.LocalDateTime;

public record SeatResponse(
    Long subjectId,
    Integer seatCount,
    LocalDateTime queryTime
) {

    public static SeatResponse from(SeatDto seatDto) {
        return new SeatResponse(
            seatDto.getSubject().getId(),
            seatDto.getSeatCount(),
            seatDto.getQueryTime()
        );
    }
}
