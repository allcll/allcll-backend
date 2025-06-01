package kr.allcll.backend.domain.seat;

import java.time.LocalDate;
import java.util.List;
import kr.allcll.backend.domain.seat.dto.PreSeatsResponse;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.seat.pin.Pin;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final PinRepository pinRepository;
    private final SeatStorage seatStorage;

    public PreSeatsResponse getAllPreSeats() {
        List<Seat> allByCreatedDate = seatRepository.findAllByCreatedDate(LocalDate.now().minusDays(1));
        return PreSeatsResponse.from(allByCreatedDate);
    }

    public List<SeatDto> getPinSeats(String token) {
        List<Pin> pins = pinRepository.findAllByToken(token, Semester.getCodeValue(LocalDate.now()));
        List<Subject> subjects = pins.stream()
            .map(Pin::getSubject)
            .toList();
        return seatStorage.getSeats(subjects);
    }
}
