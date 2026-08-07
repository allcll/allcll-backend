package kr.allcll.backend.support.scheduler;

import kr.allcll.backend.domain.seat.GeneralSeatSender;
import kr.allcll.backend.domain.seat.PinSeatSender;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import kr.allcll.backend.support.scheduler.dto.SeatSchedulerStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final GeneralSeatSender generalSeatSender;
    private final PinSeatSender pinSeatSender;
    private final SeatPipelineMetrics seatPipelineMetrics;

    public void startScheduling() {
        generalSeatSender.send();
        pinSeatSender.send();
        seatPipelineMetrics.recordSeatSseSchedulerStarted();
    }

    public void cancelScheduling() {
        generalSeatSender.cancel();
        pinSeatSender.cancel();
        seatPipelineMetrics.recordSeatSseSchedulerStopped();
    }

    public SeatSchedulerStatusResponse getSeatSchedulerStatus() {
        boolean isSending = generalSeatSender.hasActiveSchedule() && pinSeatSender.hasActiveSchedule();
        return SeatSchedulerStatusResponse.of(isSending);
    }
}
