package kr.allcll.seatfinder.seat.dto;

import java.time.LocalDateTime;
import kr.allcll.seatfinder.subject.Subject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatDto {

    private Subject subject;
    private int seatCount;
    private LocalDateTime queryTime;
}
