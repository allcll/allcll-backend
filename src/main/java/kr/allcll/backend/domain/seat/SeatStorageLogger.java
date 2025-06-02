package kr.allcll.backend.domain.seat;

import java.util.stream.Collectors;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.subject.Subject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeatStorageLogger {

    private final SeatStorage seatStorage;

    public SeatStorageLogger(SeatStorage seatStorage) {
        this.seatStorage = seatStorage;
    }

    @Scheduled(fixedRate = 10000)
    public void logSeats() {
        String jsonArray = seatStorage.getAll().stream()
            .map(this::toJson)
            .collect(Collectors.joining(",", "[", "]"));

        log.info("[SEAT_STORAGE_METRICS] {}", jsonArray);
    }

    private String toJson(SeatDto seat) {
        Subject subject = seat.getSubject();
        return String.format(
            """
                {
                    "subjectId":%d,
                    "curiNo":"%s",
                    "class":"%s",
                    "seatCount":%d,
                    "queryTime":"%s"
                }
                """,
            subject.getId(),
            subject.getCuriNo(),
            subject.getClassName(),
            seat.getSeatCount(),
            seat.getQueryTime()
        );
    }
}
