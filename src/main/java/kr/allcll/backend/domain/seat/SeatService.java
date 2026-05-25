package kr.allcll.backend.domain.seat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.seat.pin.Pin;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeatService {

    private final PinRepository pinRepository;
    private final SeatStorage seatStorage;

    public List<SeatDto> getPinSeats(String token) {
        List<Pin> pins = pinRepository.findAllByToken(token, Semester.getCurrentSemester());
        List<Subject> subjects = pins.stream()
            .map(Pin::getSubject)
            .toList();
        return seatStorage.getSeats(subjects);
    }

    public Map<String, List<SeatDto>> getPinSeatsByTokens(List<String> tokens) {
        if (tokens.isEmpty()) {
            return Map.of();
        }
        Set<String> activeTokens = Set.copyOf(tokens);

        Map<String, List<Subject>> subjectsByToken = pinRepository.findAllBySemesterAt(Semester.getCurrentSemester())
            .stream()
            .filter(pin -> activeTokens.contains(pin.getToken()))
            .collect(Collectors.groupingBy(
                Pin::getToken,
                Collectors.mapping(Pin::getSubject, Collectors.toList())
            ));

        return subjectsByToken.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> seatStorage.getSeats(entry.getValue())
            ));
    }
}
