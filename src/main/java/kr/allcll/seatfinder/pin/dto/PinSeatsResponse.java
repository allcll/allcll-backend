package kr.allcll.seatfinder.pin.dto;

import java.util.List;
import kr.allcll.seatfinder.seat.dto.SeatDto;

public record PinSeatsResponse(
    List<PinSeatResponse> seatResponses
) {

    public static PinSeatsResponse from(List<SeatDto> seatDtos) {
        return new PinSeatsResponse(seatDtos.stream()
            .map(PinSeatResponse::from)
            .toList());
    }
}
