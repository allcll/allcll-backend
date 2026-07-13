package kr.allcll.backend.domain.seat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import kr.allcll.backend.support.scheduler.ScheduledTaskHandler;
import kr.allcll.backend.support.sse.SseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeneralSeatSenderUnitTest {

    @Mock
    private SseService sseService;

    @Mock
    private SeatStorage seatStorage;

    @Mock
    private ScheduledTaskHandler scheduledTaskHandler;

    @Test
    @DisplayName("교양 여석 전송은 이전 tick 종료 후 고정 지연으로 다음 tick을 예약한다.")
    void sendWithFixedDelay() {
        // given
        GeneralSeatSender generalSeatSender = new GeneralSeatSender(sseService, seatStorage, scheduledTaskHandler);

        // when
        generalSeatSender.send();

        // then
        verify(scheduledTaskHandler).scheduleWithFixedDelay(any(Runnable.class), eq(Duration.ofSeconds(3)));
        verify(scheduledTaskHandler, never()).scheduleAtFixedRate(any(Runnable.class), any(Duration.class));
    }

    @Test
    @DisplayName("이미 전송 스케줄이 있으면 추가 등록하지 않는다.")
    void preventDuplicateSendingSchedule() {
        // given
        given(scheduledTaskHandler.getTaskCount()).willReturn(1);
        GeneralSeatSender generalSeatSender = new GeneralSeatSender(sseService, seatStorage, scheduledTaskHandler);

        // when
        generalSeatSender.send();

        // then
        verify(scheduledTaskHandler, never()).scheduleWithFixedDelay(any(Runnable.class), any(Duration.class));
        verify(scheduledTaskHandler, never()).scheduleAtFixedRate(any(Runnable.class), any(Duration.class));
    }
}
