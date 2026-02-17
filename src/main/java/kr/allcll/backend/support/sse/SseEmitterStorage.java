package kr.allcll.backend.support.sse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SseEmitterStorage {

    /**
     * token 당 단일 emitter를 저장한다.
     * ConcurrentHashMap을 사용해 멀티 스레드 환경에서도 put/remove/get을 안전하게 처리한다.
     */
    private final ConcurrentMap<String, SseEmitter> emitters;

    public SseEmitterStorage() {
        this.emitters = new ConcurrentHashMap<>();
    }

    public void add(String token, SseEmitter sseEmitter) {
        SseEmitter previousEmitter = emitters.put(token, sseEmitter);
        log.debug("[SSE] 새로운 연결이 추가되었습니다. token: {}, 현재 연결 수: {}", token, emitters.size());

        sseEmitter.onTimeout(() -> removeIfSameEmitter(token, sseEmitter, "timeout"));
        sseEmitter.onError(error -> removeIfSameEmitter(token, sseEmitter, "error", error));
        sseEmitter.onCompletion(() -> removeIfSameEmitter(token, sseEmitter, "completion"));

        if (previousEmitter != null) {
            log.debug("[SSE] 기존 연결을 교체합니다. token: {}", token);
            previousEmitter.completeWithError(new IllegalStateException("SSE emitter replaced by newer connection."));
        }
    }

    private void removeIfSameEmitter(String token, SseEmitter expectedEmitter, String reason) {
        boolean removed = emitters.remove(token, expectedEmitter);
        log.debug("[SSE] 연결 종료 콜백 처리. reason: {}, token: {}, removed: {}, 현재 연결 수: {}",
            reason, token, removed, emitters.size());
    }

    private void removeIfSameEmitter(String token, SseEmitter expectedEmitter, String reason, Throwable throwable) {
        removeIfSameEmitter(token, expectedEmitter, reason);
        if (throwable != null) {
            log.debug("[SSE] 종료 원인. token: {}, reason: {}, errorType: {}, message: {}",
                token, reason, throwable.getClass().getSimpleName(), throwable.getMessage());
        }
    }

    /**
     * 순회 중 구조 변경(연결 추가/제거) 영향 최소화를 위해 스냅샷을 반환한다.
     */
    public List<SseEmitter> getEmitters() {
        return new ArrayList<>(emitters.values());
    }

    public Optional<SseEmitter> getEmitter(String token) {
        return Optional.ofNullable(emitters.get(token));
    }

    public List<String> getUserTokens() {
        return new ArrayList<>(emitters.keySet());
    }

    public int getActiveConnectionCount() {
        return emitters.size();
    }
}
