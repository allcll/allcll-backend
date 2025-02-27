package kr.allcll.backend.seat;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PinRemainSeat(
    @JsonProperty("subjectId") Long subjectId,
    @JsonProperty("totalSeat") int totalSeat,
    @JsonProperty("remainSeat") int remainSeat,
    @JsonProperty("takenSeat") int takenSeat
) {

}
