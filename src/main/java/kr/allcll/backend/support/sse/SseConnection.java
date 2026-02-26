package kr.allcll.backend.support.sse;

import java.time.LocalDateTime;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

final class SseConnection {

    private SseEmitter emitter;
    private final LocalDateTime lastActiveAt;

    SseConnection(SseEmitter emitter, LocalDateTime lastActiveAt) {
        this.emitter = emitter;
        this.lastActiveAt = lastActiveAt;
    }

    static SseConnection of(SseEmitter emitter) {
        return new SseConnection(emitter, LocalDateTime.now());
    }

    SseEmitter getEmitter() {
        return emitter;
    }

    boolean isConnected() {
        return emitter != null;
    }

    boolean isExpired(LocalDateTime threshold) {
        return lastActiveAt.isBefore(threshold);
    }

    boolean shouldCleanup(LocalDateTime threshold) {
        return isExpired(threshold) && !isConnected();
    }

    void disconnect() {
        this.emitter = null;
    }
}
