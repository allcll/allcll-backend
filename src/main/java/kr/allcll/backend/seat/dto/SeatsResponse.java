package kr.allcll.backend.seat.dto;

import java.util.List;

public record SeatsResponse(
    List<SeatResponse> seatResponses
) {

    public static SeatsResponse from(List<SeatDto> seatDtos) {
        return new SeatsResponse(seatDtos.stream()
            .map(SeatResponse::from)
            .toList());
    }
}
