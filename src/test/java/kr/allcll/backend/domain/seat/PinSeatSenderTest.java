package kr.allcll.backend.domain.seat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.support.schedule.ScheduledTaskHandler;
import kr.allcll.backend.support.semester.Semester;
import kr.allcll.backend.support.sse.SseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class PinSeatSenderTest {

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private SeatStorage seatStorage;

    @MockitoBean
    private PinRepository pinRepository;

    @MockitoSpyBean
    @Qualifier("pinSeatTaskHandler")
    private ScheduledTaskHandler scheduledTaskHandler;

    @Autowired
    private PinSeatSender pinSeatSender;

    @Test
    @DisplayName("핀 과목 여석을 전송한다.")
    void sendPinSeats() throws InterruptedException {
        // given
        String token = "TEST_TOKEN";
        when(sseService.isDisconnected(token)).thenReturn(false);
        when(pinRepository.findAllByToken(token, Semester.now())).thenReturn(List.of());
        when(seatStorage.getSeats(any())).thenReturn(List.of());

        // when
        pinSeatSender.send(token);
        Thread.sleep(1000);

        // then
        verify(sseService, atLeastOnce()).propagate(any(), any(), any());
    }

    @Test
    @DisplayName("핀 과목 여석 전송을 취소한다.")
    void cancelPinSeatSending() throws InterruptedException {
        // given
        String token = "TEST_TOKEN";
        when(sseService.isDisconnected(token)).thenReturn(false).thenReturn(true);
        when(pinRepository.findAllByToken(token, Semester.now())).thenReturn(List.of());
        when(seatStorage.getSeats(any())).thenReturn(List.of());

        // when
        pinSeatSender.send(token);
        Thread.sleep(2500);

        // then
        verify(sseService, atLeastOnce()).propagate(any(), any(), any());
        verify(scheduledTaskHandler, times(1)).cancel(token);
    }
}
