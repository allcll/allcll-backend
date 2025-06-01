package kr.allcll.backend.domain.seat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.subject.Subject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeatStorage {

    private final Map<Subject, SeatDto> seats;

    public SeatStorage() {
        this.seats = new ConcurrentHashMap<>();
    }

    public List<SeatDto> getGeneralSeats(int limit) {
        Collection<SeatDto> seatsValue = seats.values();
        return seatsValue.stream()
//            .filter(seat -> seat.getSubject().isNonMajor())
            .filter(seat -> seat.getSeatCount() > 0)
            .sorted(Comparator.comparingInt(SeatDto::getSeatCount))
            .limit(limit)
            .toList();
    }

    public List<SeatDto> getSeats(List<Subject> subjects) {
        List<SeatDto> seats = new ArrayList<>();
        subjects.forEach(subject -> findSeat(subject).ifPresentOrElse(
            seats::add,
            () -> logSubjectMissing(subject)
        ));
        return seats;
    }

    private Optional<SeatDto> findSeat(Subject subject) {
        return Optional.ofNullable(seats.get(subject));
    }

    private void logSubjectMissing(Subject subject) {
        log.warn("[SeatStorage] 여석 정보가 존재하지 않습니다. subjectId={}, subjectName={}", subject.getId(), subject.getCuriNm());
    }

    public void add(SeatDto seatDto) {
        seats.put(seatDto.getSubject(), seatDto);
    }

    public void addAll(List<SeatDto> seatDtos) {
        seatDtos.forEach(this::add);
    }

    public void clear() {
        seats.clear();
    }
}
