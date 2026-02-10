package kr.allcll.backend.domain.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;

import kr.allcll.backend.support.sse.SseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class GeneralSeatSenderTest {

    @MockitoBean
    private SseService sseService;

    @Autowired
    private GeneralSeatSender generalSeatSender;

    @AfterEach
    void tearDown() {
        generalSeatSender.cancel();
    }

    @Test
    @DisplayName("교양 여석을 전송한다.")
    void sendTest() throws InterruptedException {
        // when
        generalSeatSender.send();
        Thread.sleep(1000); // wait for period

        // then
        verify(sseService, atLeastOnce()).propagate(any(), any());
    }

    @Test
    @DisplayName("6초 동안 3초 간격으로 교양 여석을 전송한다.")
    void sendPeriodicallyTest() throws InterruptedException {
        // given
        int period = 2;

        // when
        generalSeatSender.send();
        Thread.sleep(period * 3000); // wait for period
        generalSeatSender.cancel();
        Thread.sleep(1000); // wait for cancel

        // then
        verify(sseService, atLeast(period)).propagate(any(), any());
    }

    @Test
    @DisplayName("중복 여석 전송을 방지한다.")
    void preventDuplicateSendingTest() throws InterruptedException {
        // given
        int period = 2;
        generalSeatSender.send();
        Thread.sleep(100); // wait for first send

        // when
        generalSeatSender.send();
        Thread.sleep(period * 1000); // wait for period

        // then
        int buffer = 1;
        verify(sseService, Mockito.atMost(period + buffer)).propagate(any(), any());
    }

    @Test
    @DisplayName("교양 여석 전송을 취소한다.")
    void cancelTest() throws InterruptedException {
        // given
        generalSeatSender.send();

        // when
        generalSeatSender.cancel();
        Thread.sleep(1000); // wait for cancel
        int previousCalls = getMethodCallCount(sseService, "propagate");
        Thread.sleep(2000); // wait for no more calls

        // then
        int afterCancelCalls = getMethodCallCount(sseService, "propagate");
        assertThat(afterCancelCalls).isEqualTo(previousCalls);
    }

    private int getMethodCallCount(Object object, String methodName) {
        return mockingDetails(object).getInvocations().stream()
            .filter(invocation -> invocation.getMethod().getName().equals(methodName))
            .mapToInt(invocation -> 1)
            .sum();
    }
}
