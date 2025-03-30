package kr.allcll.backend.domain.seat;

import java.time.Duration;
import java.util.List;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.seat.pin.Pin;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.seat.pin.dto.PinSeatsResponse;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.support.schedule.ScheduledTaskHandler;
import kr.allcll.backend.support.sse.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PinSeatSender {

    private static final String PIN_EVENT_NAME = "pinSeats";
    private static final int TASK_DURATION = 1000;

    private final SseService sseService;
    private final SeatStorage seatStorage;
    private final PinRepository pinRepository;
    private final ScheduledTaskHandler scheduledTaskHandler;

    public PinSeatSender(
        SseService sseService,
        SeatStorage seatStorage,
        PinRepository pinRepository,
        @Qualifier("pinSeatTaskHandler") ScheduledTaskHandler scheduledTaskHandler
    ) {
        this.sseService = sseService;
        this.seatStorage = seatStorage;
        this.pinRepository = pinRepository;
        this.scheduledTaskHandler = scheduledTaskHandler;
    }

    public void send(String token) {
        scheduledTaskHandler.scheduleAtFixedRate(token, getMajorSeatTask(token), Duration.ofMillis(TASK_DURATION));
    }

    private Runnable getMajorSeatTask(String token) {
        return () -> {
            if (sseService.isDisconnected(token)) {
                scheduledTaskHandler.cancel(token);
                return;
            }
            List<Pin> pins = pinRepository.findAllByToken(token);
            List<Subject> subjects = pins.stream()
                .map(Pin::getSubject)
                .toList();
            List<SeatDto> pinSeats = seatStorage.getSeats(subjects);
            sseService.propagate(token, PIN_EVENT_NAME, PinSeatsResponse.from(pinSeats));
        };
    }
}
