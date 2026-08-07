package kr.allcll.backend.support.scheduler;

import static org.mockito.Mockito.verify;

import kr.allcll.backend.domain.seat.GeneralSeatSender;
import kr.allcll.backend.domain.seat.PinSeatSender;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private GeneralSeatSender generalSeatSender;

    @Mock
    private PinSeatSender pinSeatSender;

    @Mock
    private SeatPipelineMetrics seatPipelineMetrics;

    @InjectMocks
    private SchedulerService schedulerService;

    @Test
    @DisplayName("SSE 스케줄러를 시작하면 활성 상태 메트릭을 기록한다.")
    void startSchedulingRecordsActiveMetric() {
        // when
        schedulerService.startScheduling();

        // then
        verify(generalSeatSender).send();
        verify(pinSeatSender).send();
        verify(seatPipelineMetrics).recordSeatSseSchedulerStarted();
    }

    @Test
    @DisplayName("SSE 스케줄러를 중지하면 활성 상태 메트릭을 기록한다.")
    void cancelSchedulingRecordsInactiveMetric() {
        // when
        schedulerService.cancelScheduling();

        // then
        verify(generalSeatSender).cancel();
        verify(pinSeatSender).cancel();
        verify(seatPipelineMetrics).recordSeatSseSchedulerStopped();
    }
}
