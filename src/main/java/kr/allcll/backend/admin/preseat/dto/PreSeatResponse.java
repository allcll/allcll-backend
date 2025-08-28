package kr.allcll.backend.admin.preseat.dto;

public record PreSeatResponse(
    Long subjectId,
    Integer seat
) {

    public static PreSeatResponse of(Long subjectId, Integer remainSeat) {
        return new PreSeatResponse(subjectId, remainSeat);
    }

    public static PreSeatResponse from(PreSeatResponse preSeat) {
        return new PreSeatResponse(preSeat.subjectId, preSeat.seat());
    }
}
