package kr.allcll.backend.domain.seat;

import java.time.Duration;
import java.util.List;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.seat.dto.SeatsResponse;
import kr.allcll.backend.support.schedule.ScheduledTaskHandler;
import kr.allcll.backend.support.sse.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeneralSeatSender {

    private static final String NON_MAJOR_SEATS_EVENT_NAME = "nonMajorSeats";
    private static final int NON_MAJOR_SUBJECT_QUERY_LIMIT = 20;
    private static final Duration SENDING_PERIOD = Duration.ofSeconds(1);

    private final SseService sseService;
    private final SeatStorage seatStorage;
    private final ScheduledTaskHandler scheduledTaskHandler;

    public GeneralSeatSender(
        SseService sseService,
        SeatStorage seatStorage,
        @Qualifier("generalSeatTaskHandler") ScheduledTaskHandler scheduledTaskHandler
    ) {
        this.sseService = sseService;
        this.seatStorage = seatStorage;
        this.scheduledTaskHandler = scheduledTaskHandler;
    }

    public void send() {
        if (scheduledTaskHandler.getTaskCount() > 0) {
            return;
        }
        scheduledTaskHandler.scheduleAtFixedRate(getNonMajorSeatTask(), SENDING_PERIOD);
    }

    public void cancel() {
        scheduledTaskHandler.cancelAll();
    }

    private Runnable getNonMajorSeatTask() {
        return () -> {
            List<SeatDto> nonMajorSeats = seatStorage.getNonMajorSeats(NON_MAJOR_SUBJECT_QUERY_LIMIT);
            sseService.propagate(NON_MAJOR_SEATS_EVENT_NAME, SeatsResponse.from(nonMajorSeats));
        };
    }
}
