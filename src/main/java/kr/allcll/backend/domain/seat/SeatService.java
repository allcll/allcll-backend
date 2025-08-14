package kr.allcll.backend.domain.seat;

import java.util.List;
import kr.allcll.backend.domain.seat.dto.PreSeatsResponse;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.seat.pin.Pin;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.support.semester.Semester;
import kr.allcll.crawler.seat.AllPreSeatBuffer;
import kr.allcll.crawler.seat.PreSeatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final PinRepository pinRepository;
    private final SeatStorage seatStorage;
    private final AllPreSeatBuffer allPreSeatBuffer;

    public PreSeatsResponse getAllPreSeats() {
        List<PreSeatResponse> preSeats = allPreSeatBuffer.getAllAndFlush();
        return PreSeatsResponse.from(preSeats);
    }

    public List<SeatDto> getPinSeats(String token) {
        List<Pin> pins = pinRepository.findAllByToken(token, Semester.now());
        List<Subject> subjects = pins.stream()
            .map(Pin::getSubject)
            .toList();
        return seatStorage.getSeats(subjects);
    }
}
