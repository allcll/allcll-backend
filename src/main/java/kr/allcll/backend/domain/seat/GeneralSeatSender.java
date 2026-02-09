package kr.allcll.backend.domain.seat;

import java.time.Duration;
import java.util.List;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.seat.dto.SeatsResponse;
import kr.allcll.backend.support.scheduler.ScheduledTaskHandler;
import kr.allcll.backend.support.sse.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeneralSeatSender {

    private static final String EVENT_NAME = "nonMajorSeats";
    private static final int QUERY_LIMIT = 20;
    private static final int SEASON_SEMESTER_QUERY_LIMIT = 40; // 2025-동계 전체 제공을 위한 쿼리 제한 수
    private static final Duration SENDING_PERIOD = Duration.ofSeconds(3);

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
        scheduledTaskHandler.scheduleAtFixedRate(getAllSeatTaskAtSeasonSemester(), SENDING_PERIOD);
    }

    public boolean hasActiveSchedule() {
        return scheduledTaskHandler.getTaskCount() > 0;
    }

    private Runnable getGeneralSeatTask() {
        return () -> {
            List<SeatDto> generalSeats = seatStorage.getGeneralSeats(QUERY_LIMIT);
            sseService.propagate(EVENT_NAME, SeatsResponse.from(generalSeats));
        };
    }

    private Runnable getAllSeatTaskAtSeasonSemester() {
        return () -> {
            List<SeatDto> allSeat = seatStorage.getAllSeatsAtSeasonSemester(SEASON_SEMESTER_QUERY_LIMIT);
            sseService.propagate(EVENT_NAME, SeatsResponse.from(allSeat));
        };
    }

    public void cancel() {
        scheduledTaskHandler.cancelAll();
    }
}
