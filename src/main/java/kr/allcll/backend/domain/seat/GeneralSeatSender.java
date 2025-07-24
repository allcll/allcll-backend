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

    private static final String EVENT_NAME = "nonMajorSeats";
    private static final int QUERY_LIMIT = 40;
    private static final int SEASON_SEMESTER_QUERY_LIMIT = 39; // 2025-하계 전체 제공을 위한 쿼리 제한 수
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
        if (hasActiveSchedule()) {
            return;
        }
        scheduledTaskHandler.scheduleAtFixedRate(getGeneralSeatTask(), SENDING_PERIOD);
    }

    private boolean hasActiveSchedule() {
        return scheduledTaskHandler.getTaskCount() > 0;
    }

    private Runnable getGeneralSeatTask() {
        return () -> {
            List<SeatDto> generalSeats = seatStorage.getGeneralSeats();
            sseService.propagate(EVENT_NAME, SeatsResponse.from(generalSeats));
        };
    }

    public void cancel() {
        scheduledTaskHandler.cancelAll();
    }
}
