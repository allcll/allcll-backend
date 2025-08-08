package kr.allcll.backend.domain.seat;

import java.time.Duration;
import java.util.List;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.seat.pin.dto.PinSeatsResponse;
import kr.allcll.backend.support.scheduler.ScheduledTaskHandler;
import kr.allcll.backend.support.sse.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PinSeatSender {

    private static final String PIN_EVENT_NAME = "pinSeats";
    private static final Duration SENDING_PERIOD = Duration.ofSeconds(1);

    private final SseService sseService;
    private final SeatService seatService;
    private final ScheduledTaskHandler scheduledTaskHandler;

    public PinSeatSender(
        SseService sseService,
        SeatService seatService,
        @Qualifier("pinSeatTaskHandler") ScheduledTaskHandler scheduledTaskHandler
    ) {
        this.sseService = sseService;
        this.seatService = seatService;
        this.scheduledTaskHandler = scheduledTaskHandler;
    }

    public void send() {
        if (hasActiveSchedule()) {
            return;
        }
        scheduledTaskHandler.scheduleAtFixedRate(getPinSeatTask(), SENDING_PERIOD);
    }

    public boolean hasActiveSchedule() {
        return scheduledTaskHandler.getTaskCount() > 0;
    }

    private Runnable getPinSeatTask() {
        return () -> {
            List<String> tokens = sseService.getConnectedTokens();
            tokens.forEach(token -> {
                List<SeatDto> pinSeats = seatService.getPinSeats(token);
                sseService.propagate(token, PIN_EVENT_NAME, PinSeatsResponse.from(pinSeats));
            });
        };
    }

    public void cancel() {
        scheduledTaskHandler.cancelAll();
    }
}
