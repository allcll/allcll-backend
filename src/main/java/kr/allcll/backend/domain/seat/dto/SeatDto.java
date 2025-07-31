package kr.allcll.backend.domain.seat.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.subject.Subject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatDto {

    private Subject subject;
    private int seatCount;
    private LocalDateTime queryTime;
//    private ChangeStatus changeStatus;
}
