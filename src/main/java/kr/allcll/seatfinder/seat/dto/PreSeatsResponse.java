package kr.allcll.seatfinder.seat.dto;

import java.util.List;
import kr.allcll.seatfinder.seat.Seat;

public record PreSeatsResponse(
    List<PreSeatResponse> subjects
) {

    public static PreSeatsResponse from(List<Seat> allSeats) {
        List<PreSeatResponse> result = allSeats.stream()
            .map(PreSeatResponse::from)
            .toList();
        return new PreSeatsResponse(result);
    }
}
