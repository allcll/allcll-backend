package kr.allcll.backend.support.sse;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

class SseServiceMetricsTest {

    @Test
    @DisplayName("SSE 전송 실패 시 failure counter가 증가한다.")
    void sseSendFailureCount() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        SeatPipelineMetrics seatPipelineMetrics = new SeatPipelineMetrics(meterRegistry);
        SseEmitterStorage sseEmitterStorage = new SseEmitterStorage();
        SseService sseService = new SseService(sseEmitterStorage, seatPipelineMetrics);
        sseEmitterStorage.add("token", new FailingSseEmitter());

        // when
        sseService.propagate("token", "message", "data");

        // then
        assertThat(meterRegistry.get("sse.send.failure.count")
            .counter()
            .count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("sse.send.duration")
            .timer()
            .count()).isEqualTo(1);
    }

    private static class FailingSseEmitter extends SseEmitter {

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            throw new IOException("send failed");
        }
    }
}
