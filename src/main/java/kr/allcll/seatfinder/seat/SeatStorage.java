package kr.allcll.seatfinder.seat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kr.allcll.seatfinder.subject.Subject;
import org.springframework.stereotype.Component;

@Component
public class SeatStorage {

    private final Map<Long, Seat> seats;

    public SeatStorage() {
        this.seats = new ConcurrentHashMap<>();
    }

    public List<Seat> getNonMajorSeats(int limit) {
        Collection<Seat> seatsValue = seats.values();
        return seatsValue.stream()
            .filter(seat -> seat.getSubject().isNonMajor())
            .sorted((s1, s2) -> s2.getSeatCount() - s1.getSeatCount())
            .limit(limit)
            .toList();
    }

    public List<Seat> getSeats(List<Subject> subjects) {
        List<Seat> result = new ArrayList<>();
        for (Subject subject : subjects) {
            for (Seat seat : seats.values()) {
                if (seat.getSubject().getId().equals(subject.getId())) {
                    result.add(seat);
                    break;
                }
            }
        }
        return result;
    }

    public void add(Seat seat) {
        seats.put(seat.getSubject().getId(), seat);
    }

    public void addAll(List<Seat> seats) {
        for (Seat seat : seats) {
            this.seats.put(seat.getSubject().getId(), seat);
        }
    }
}
