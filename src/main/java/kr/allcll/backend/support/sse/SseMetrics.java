package kr.allcll.backend.support.sse;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseMetrics {

    private final MeterRegistry meterRegistry;
    private final SseEmitterStorage sseEmitterStorage;

    @PostConstruct
    public void init() {
        meterRegistry.gauge("sse.active.connections",
            Tags.of("app", "backend"),
            sseEmitterStorage,
            SseEmitterStorage::getActiveConnectionCount);
    }
}
