package kr.allcll.backend.domain.seat;

import java.util.List;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.seat.pin.Pin;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final PinRepository pinRepository;
    private final SeatStorage seatStorage;

    public List<SeatDto> getPinSeats(String token) {
        List<Pin> pins = pinRepository.findAllByToken(token, Semester.now());
        List<Subject> subjects = pins.stream()
            .map(Pin::getSubject)
            .toList();
        return seatStorage.getSeats(subjects);
    }
}
