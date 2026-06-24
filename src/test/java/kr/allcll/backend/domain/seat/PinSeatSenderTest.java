package kr.allcll.backend.domain.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.services.sheets.v4.Sheets;
import java.time.Duration;
import java.util.List;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.support.semester.Semester;
import kr.allcll.backend.support.sse.SseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class PinSeatSenderTest {

    private static final Duration SENDING_PERIOD = Duration.ofSeconds(1);
    private static final Duration VERIFY_TIMEOUT = SENDING_PERIOD.plusSeconds(2);
    private static final Duration NO_ADDITIONAL_SEND_DURATION = SENDING_PERIOD.plusMillis(500);

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private Sheets sheets;

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
        String token = "TEST_TOKEN";
        when(sseService.getConnectedTokens()).thenReturn(List.of(token));
        when(pinRepository.findAllByToken(token, Semester.getCurrentSemester())).thenReturn(List.of());
        when(seatStorage.getSeats(any())).thenReturn(List.of());

        // when
        pinSeatSender.send();

        // then
        awaitPropagation(atLeastOnce());
    }

    @Test
    @DisplayName("핀 과목 여석 전송을 취소한다.")
    void cancelPinSeatSending() {
        // given
        String token = "TEST_TOKEN";
        when(sseService.getConnectedTokens()).thenReturn(List.of(token));
        when(pinRepository.findAllByToken(token, Semester.getCurrentSemester())).thenReturn(List.of());
        when(seatStorage.getSeats(any())).thenReturn(List.of());

        // when
        pinSeatSender.send();
        awaitPropagation(atLeastOnce());
        pinSeatSender.cancel();
        int previousCalls = getMethodCallCount(sseService, "propagate");

        // then
        assertThat(pinSeatSender.hasActiveSchedule()).isFalse();
        await()
            .during(NO_ADDITIONAL_SEND_DURATION)
            .atMost(VERIFY_TIMEOUT)
            .untilAsserted(() -> assertThat(getMethodCallCount(sseService, "propagate"))
                .isEqualTo(previousCalls));
    }

    private void awaitPropagation(VerificationMode verificationMode) {
        await()
            .atMost(VERIFY_TIMEOUT)
            .untilAsserted(() -> verify(sseService, verificationMode).propagate(any(), any(), any()));
    }

    private int getMethodCallCount(Object object, String methodName) {
        return mockingDetails(object).getInvocations().stream()
            .filter(invocation -> invocation.getMethod().getName().equals(methodName))
            .mapToInt(invocation -> 1)
            .sum();
    }
}
