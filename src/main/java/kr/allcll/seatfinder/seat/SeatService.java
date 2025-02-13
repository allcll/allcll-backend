package kr.allcll.seatfinder.seat;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import kr.allcll.seatfinder.pin.Pin;
import kr.allcll.seatfinder.pin.PinRepository;
import kr.allcll.seatfinder.pin.dto.PinSeatsResponse;
import kr.allcll.seatfinder.seat.dto.PreSeatsResponse;
import kr.allcll.seatfinder.seat.dto.SeatDto;
import kr.allcll.seatfinder.seat.dto.SeatsResponse;
import kr.allcll.seatfinder.sse.SseService;
import kr.allcll.seatfinder.subject.Subject;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatService {

    private static final String NON_MAJOR_SEATS_EVENT_NAME = "nonMajorSeats";
    private static final int NON_MAJOR_SUBJECT_QUERY_LIMIT = 20;
    private static final String PIN_EVENT_NAME = "pinSeats";
    private static final int TASK_DURATION = 1000;
    private static final int TASK_PERIOD = 10000;

    private final SseService sseService;
    private final SeatStorage seatStorage;
    private final PinRepository pinRepository;
    private final ThreadPoolTaskScheduler scheduler;
    private final SeatRepository seatRepository;

    @Scheduled(fixedRate = 1000)
    public void sendNonMajorSeats() {
        List<SeatDto> nonMajorSeatDtos = seatStorage.getNonMajorSeats(NON_MAJOR_SUBJECT_QUERY_LIMIT);
        sseService.propagate(NON_MAJOR_SEATS_EVENT_NAME, SeatsResponse.from(nonMajorSeatDtos));
    }

    public void sendPinSeatsInformation(String token) {
        Runnable task = () -> {
            List<Pin> pins = pinRepository.findAllByToken(token);
            List<Subject> subjects = pins.stream()
                .map(Pin::getSubject)
                .toList();
            List<SeatDto> pinSeatDtos = seatStorage.getSeats(subjects);
            sseService.propagate(PIN_EVENT_NAME, PinSeatsResponse.from(pinSeatDtos));
        };
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(task, Duration.ofMillis(TASK_DURATION));
        scheduler.schedule(() -> scheduledFuture.cancel(true),
            new Date(System.currentTimeMillis() + TASK_PERIOD));
    }

    public PreSeatsResponse getAllPreSeats() {
        List<Seat> allByCreatedDate = seatRepository.findAllByCreatedDate(LocalDate.of(2025, 2, 14));
        return PreSeatsResponse.from(allByCreatedDate);
    }
}
