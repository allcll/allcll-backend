package kr.allcll.backend.domain.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.api.services.sheets.v4.Sheets;
import java.time.Duration;
import kr.allcll.backend.support.scheduler.ScheduledTaskHandler;
import kr.allcll.backend.support.sse.SseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class GeneralSeatSenderTest {

    private static final Duration SENDING_PERIOD = Duration.ofSeconds(3);
    private static final Duration VERIFY_TIMEOUT = SENDING_PERIOD.plusSeconds(2);
    private static final Duration NO_ADDITIONAL_SEND_DURATION = SENDING_PERIOD.plusMillis(500);

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private Sheets sheets;

    @MockitoSpyBean
    @Qualifier("generalSeatTaskHandler")
    private ScheduledTaskHandler scheduledTaskHandler;

    @Autowired
    private GeneralSeatSender generalSeatSender;

    @AfterEach
    void tearDown() {
        generalSeatSender.cancel();
    }

    @Test
    @DisplayName("교양 여석을 전송한다.")
    void sendTest() {
        // when
        generalSeatSender.send();

        // then
        awaitPropagation(atLeastOnce());
    }

    @Test
    @DisplayName("3초 간격으로 교양 여석을 반복 전송한다.")
    void sendPeriodicallyTest() {
        // given
        int expectedSendCount = 2;

        // when
        generalSeatSender.send();

        // then
        verify(scheduledTaskHandler, times(1))
            .scheduleAtFixedRate(any(Runnable.class), eq(SENDING_PERIOD));
        awaitPropagation(atLeast(expectedSendCount));
    }

    @Test
    @DisplayName("중복 여석 전송을 방지한다.")
    void preventDuplicateSendingTest() {
        // when
        generalSeatSender.send();
        generalSeatSender.send();

        // then
        assertThat(generalSeatSender.hasActiveSchedule()).isTrue();
        verify(scheduledTaskHandler, times(1))
            .scheduleAtFixedRate(any(Runnable.class), eq(SENDING_PERIOD));
    }

    @Test
    @DisplayName("교양 여석 전송을 취소한다.")
    void cancelTest() {
        // given
        generalSeatSender.send();
        awaitPropagation(atLeastOnce());

        // when
        generalSeatSender.cancel();
        int previousCalls = getMethodCallCount(sseService, "propagate");

        // then
        assertThat(generalSeatSender.hasActiveSchedule()).isFalse();
        await()
            .during(NO_ADDITIONAL_SEND_DURATION)
            .atMost(VERIFY_TIMEOUT)
            .untilAsserted(() -> assertThat(getMethodCallCount(sseService, "propagate"))
                .isEqualTo(previousCalls));
    }

    private void awaitPropagation(VerificationMode verificationMode) {
        await()
            .atMost(VERIFY_TIMEOUT)
            .untilAsserted(() -> verify(sseService, verificationMode).propagate(any(), any()));
    }

    private int getMethodCallCount(Object object, String methodName) {
        return mockingDetails(object).getInvocations().stream()
            .filter(invocation -> invocation.getMethod().getName().equals(methodName))
            .mapToInt(invocation -> 1)
            .sum();
    }
}
