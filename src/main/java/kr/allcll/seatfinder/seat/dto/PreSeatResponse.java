package kr.allcll.seatfinder.seat.dto;

import kr.allcll.seatfinder.seat.Seat;

public record PreSeatResponse(
    Long subjectId,
    int seat
) {

    public static PreSeatResponse from(Seat seat) {
        return new PreSeatResponse(seat.getSubject().getId(), Math.max(seat.getTotLimitRcnt() - seat.getTotRcnt(), 0));
    }
}
