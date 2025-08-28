package kr.allcll.backend.domain.seat.preseat.dto;

import java.util.List;
import kr.allcll.backend.admin.preseat.dto.PreSeatResponse;

public record PreSeatsResponse(
    List<PreSeatResponse> preSeats
) {

    public static PreSeatsResponse from(List<PreSeatResponse> allPreSeats) {
        List<PreSeatResponse> result = allPreSeats.stream()
            .map(PreSeatResponse::from)
            .toList();
        return new PreSeatsResponse(result);
    }
}
