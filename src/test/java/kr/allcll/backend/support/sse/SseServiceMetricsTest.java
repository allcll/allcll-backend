package kr.allcll.backend.support.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.time.Duration;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseServiceMetricsTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final SeatPipelineMetrics seatPipelineMetrics = new SeatPipelineMetrics(meterRegistry);
    private final SseEmitterStorage sseEmitterStorage = new SseEmitterStorage();
    private final SseService sseService = new SseService(sseEmitterStorage, seatPipelineMetrics);

    @AfterEach
    void tearDown() {
        sseService.shutdown();
    }

    @Test
    @DisplayName("SSE 전송 실패 시 failure counter가 증가한다.")
    void sseSendFailureCount() {
        // given
        sseEmitterStorage.add("token", new FailingSseEmitter());

        // when
        sseService.propagate("token", "message", "data");

        // then
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                Counter failureCounter = meterRegistry.find("sse.send.failure.count").counter();
                Timer sendTimer = meterRegistry.find("sse.send.duration").timer();

                assertThat(failureCounter).isNotNull();
                assertThat(failureCounter.count()).isEqualTo(1.0);
                assertThat(sendTimer).isNotNull();
                assertThat(sendTimer.count()).isEqualTo(1);
            });
    }

    private static class FailingSseEmitter extends SseEmitter {

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            throw new IOException("send failed");
        }
    }
}
