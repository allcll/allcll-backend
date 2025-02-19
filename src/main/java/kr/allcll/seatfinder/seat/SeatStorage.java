package kr.allcll.seatfinder.seat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kr.allcll.seatfinder.seat.dto.SeatDto;
import kr.allcll.seatfinder.subject.Subject;
import org.springframework.stereotype.Component;

@Component
public class SeatStorage {

    private final Map<Long, SeatDto> seats;

    public SeatStorage() {
        this.seats = new ConcurrentHashMap<>();
    }

    public List<SeatDto> getNonMajorSeats(int limit) {
        Collection<SeatDto> seatsValue = seats.values();
        return seatsValue.stream()
            .filter(seat -> seat.getSubject().isNonMajor())
            .sorted((s1, s2) -> s2.getSeatCount() - s1.getSeatCount())
            .limit(limit)
            .toList();
    }

    public List<SeatDto> getSeats(List<Subject> subjects) {
        List<SeatDto> result = new ArrayList<>();
        for (Subject subject : subjects) {
            for (SeatDto seatDto : seats.values()) {
                if (seatDto.getSubject().getId().equals(subject.getId())) {
                    result.add(seatDto);
                    break;
                }
            }
        }
        return result;
    }

    public void add(SeatDto seatDto) {
        seats.put(seatDto.getSubject().getId(), seatDto);
    }

    public void addAll(List<SeatDto> seatDtos) {
        for (SeatDto seatDto : seatDtos) {
            this.seats.put(seatDto.getSubject().getId(), seatDto);
        }
    }
}
