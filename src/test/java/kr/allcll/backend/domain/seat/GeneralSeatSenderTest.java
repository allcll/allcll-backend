package kr.allcll.backend.domain.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import kr.allcll.backend.support.scheduler.ScheduledTaskHandler;
import kr.allcll.backend.support.sse.SseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class GeneralSeatSenderTest {

    private static final Duration PERIOD = Duration.ofSeconds(3);

    @MockitoBean
    private SseService sseService;

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
        await()
            .atMost(PERIOD.plusSeconds(1))
            .untilAsserted(() -> verify(sseService, atLeastOnce()).propagate(any(), any()));
    }

    @Test
    @DisplayName("6초 동안 3초 간격으로 교양 여석을 전송한다.")
    void sendPeriodicallyTest() {
        // given
        int period = 2;

        // when
        generalSeatSender.send();

        // then
        await()
            .atMost(PERIOD.multipliedBy(period).plusSeconds(1))
            .untilAsserted(() -> verify(sseService, atLeast(period)).propagate(any(), any()));
    }

    @Test
    @DisplayName("중복 여석 전송을 방지한다.")
    void preventDuplicateSendingTest() {
        // given
        generalSeatSender.send();
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(scheduledTaskHandler.getTaskCount()).isEqualTo(1));

        // when
        generalSeatSender.send();

        // then
        assertThat(scheduledTaskHandler.getTaskCount()).isEqualTo(1);
        verify(sseService, Mockito.atMost(1)).propagate(any(), any());
    }

    @Test
    @DisplayName("교양 여석 전송을 취소한다.")
    void cancelTest() {
        // given
        generalSeatSender.send();
        await()
            .atMost(PERIOD.plusSeconds(1))
            .untilAsserted(() -> verify(sseService, atLeastOnce()).propagate(any(), any()));

        // when
        generalSeatSender.cancel();
        int previousCalls = getMethodCallCount(sseService, "propagate");

        // then
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(scheduledTaskHandler.getTaskCount()).isEqualTo(0));
        await()
            .pollDelay(PERIOD)
            .atMost(PERIOD.plusSeconds(1))
            .untilAsserted(() -> assertThat(getMethodCallCount(sseService, "propagate")).isEqualTo(previousCalls));
    }

    private int getMethodCallCount(Object object, String methodName) {
        return mockingDetails(object).getInvocations().stream()
            .filter(invocation -> invocation.getMethod().getName().equals(methodName))
            .mapToInt(invocation -> 1)
            .sum();
    }
}
