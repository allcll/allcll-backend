package kr.allcll.backend.domain.seat.dto;

import java.util.List;
import kr.allcll.crawler.seat.PreSeatResponse;

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
