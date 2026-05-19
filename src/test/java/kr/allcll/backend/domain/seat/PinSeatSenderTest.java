package kr.allcll.backend.domain.seat;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.support.semester.Semester;
import kr.allcll.backend.support.sse.SseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class PinSeatSenderTest {

    private static final Duration PERIOD = Duration.ofSeconds(1);
    private static final String TOKEN = "TEST_TOKEN";

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private SeatStorage seatStorage;

    @MockitoBean
    private PinRepository pinRepository;

    @Autowired
    private PinSeatSender pinSeatSender;

    @AfterEach
    void tearDown() {
        pinSeatSender.cancel();
    }

    @Test
    @DisplayName("핀 과목 여석을 전송한다.")
    void sendPinSeats() {
        // given
        stubPinSeatSending(List.of(TOKEN));

        // when
        pinSeatSender.send();

        // then
        await()
            .atMost(PERIOD.plusSeconds(1))
            .untilAsserted(() -> verify(sseService, atLeastOnce()).propagate(any(), any(), any()));
    }

    @Test
    @DisplayName("핀 과목 여석 전송을 취소한다.")
    void cancelPinSeatSending() {
        // given
        stubPinSeatSending(List.of(TOKEN), List.of());

        // when
        pinSeatSender.send();

        // then
        await()
            .pollDelay(PERIOD.plusMillis(500))
            .atMost(PERIOD.plusSeconds(1))
            .untilAsserted(() -> verify(sseService, times(1)).propagate(any(), any(), any()));
    }

    private void stubPinSeatSending(List<String>... connectedTokens) {
        if (connectedTokens.length == 1) {
            when(sseService.getConnectedTokens()).thenReturn(connectedTokens[0]);
        } else {
            when(sseService.getConnectedTokens()).thenReturn(
                connectedTokens[0],
                Arrays.copyOfRange(connectedTokens, 1, connectedTokens.length)
            );
        }
        when(pinRepository.findAllByToken(TOKEN, Semester.getCurrentSemester())).thenReturn(List.of());
        when(seatStorage.getSeats(any())).thenReturn(List.of());
    }
}
