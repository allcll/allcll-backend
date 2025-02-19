package kr.allcll.seatfinder.seat;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import kr.allcll.seatfinder.pin.Pin;
import kr.allcll.seatfinder.pin.PinRepository;
import kr.allcll.seatfinder.pin.dto.PinSeatsResponse;
import kr.allcll.seatfinder.seat.dto.SeatDto;
import kr.allcll.seatfinder.seat.dto.SeatsResponse;
import kr.allcll.seatfinder.sse.SseService;
import kr.allcll.seatfinder.subject.Subject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private static final String NON_MAJOR_SEATS_EVENT_NAME = "nonMajorSeats";
    private static final String PIN_EVENT_NAME = "pinSeats";
    private static final int NON_MAJOR_SUBJECT_QUERY_LIMIT = 20;
    private static final int TASK_DURATION = 1000;
    private static final int TASK_PERIOD = 60000;

    private final SseService sseService;
    private final SeatStorage seatStorage;
    private final PinRepository pinRepository;
    private final SeatRepository seatRepository;
    private final ThreadPoolTaskScheduler scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 1000)
    public void sendNonMajorSeats() {
        List<SeatDto> nonMajorSeatDtos = seatStorage.getNonMajorSeats(NON_MAJOR_SUBJECT_QUERY_LIMIT);
        sseService.propagate(NON_MAJOR_SEATS_EVENT_NAME, SeatsResponse.from(nonMajorSeatDtos));
    }

    public void sendPinSeatsInformation(String token) {
        if (scheduledTasks.containsKey(token)) {
            log.info("토큰 {} 에 대해 이미 스케줄된 작업이 존재합니다.", token);
            return;
        }

        Runnable task = () -> {
            List<Pin> pins = pinRepository.findAllByToken(token);
            List<Subject> subjects = pins.stream()
                .map(Pin::getSubject)
                .toList();
            List<SeatDto> pinSeatDtos = seatStorage.getSeats(subjects);
            sseService.propagate(token, PIN_EVENT_NAME, PinSeatsResponse.from(pinSeatDtos));
        };

        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(task, Duration.ofMillis(TASK_DURATION));
        scheduledTasks.put(token, scheduledFuture);

        scheduler.schedule(() -> {
                log.info("토큰 {}: 태스크 종료", token);
                scheduledFuture.cancel(true);
                scheduledTasks.remove(token);
            },
            new Date(System.currentTimeMillis() + TASK_PERIOD));
    }
}
