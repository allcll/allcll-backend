package kr.allcll.backend.domain.seat;

import java.time.LocalDate;
import java.util.List;
import kr.allcll.backend.domain.seat.dto.PreSeatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    public PreSeatsResponse getAllPreSeats() {
        List<Seat> allByCreatedDate = seatRepository.findAllByCreatedDate((LocalDate.of(2025, 2, 28)));
        return PreSeatsResponse.from(allByCreatedDate);
    }
}
