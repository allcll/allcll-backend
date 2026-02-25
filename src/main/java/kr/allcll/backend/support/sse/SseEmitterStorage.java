package kr.allcll.backend.support.sse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SseEmitterStorage {

    private static final Duration GRACE_PERIOD = Duration.ofSeconds(30);

    /*
        ExecutorService로 변경이 필요할 수 있습니다.
     */
    private final Map<String, SseEmitter> emitters;

    /*
        SSE 연결이 끊긴 후에도 일정 시간 동안 크롤링 대상으로 유지하기 위한 마지막 활성 시각
     */
    private final Map<String, LocalDateTime> lastActiveTimes;

    /*
        동시성 문제로 자료구조 변경이 필요할 수 있습니다.
     */
    public SseEmitterStorage() {
        this.emitters = new ConcurrentHashMap<>();
        this.lastActiveTimes = new ConcurrentHashMap<>();
    }

    public void add(String token, SseEmitter sseEmitter) {
        emitters.put(token, sseEmitter);
        lastActiveTimes.put(token, LocalDateTime.now());
        log.info("[SSE] 새로운 연결이 추가되었습니다. 현재 연결 수: {}", emitters.size());
        sseEmitter.onTimeout(() -> {
            emitters.remove(token);
            log.debug("[SSE] 연결이 타임아웃으로 종료되었습니다. 현재 연결 수: {}", emitters.size());
        });
        sseEmitter.onError(e -> {
            emitters.remove(token);
        });
        sseEmitter.onCompletion(() -> {
            emitters.remove(token);
        });
    }

    /*
        실제 객체를 전달하지 않으면 이미 완료된 SseEmitter에 대해 send() 호출이 발생한다.
        Collections.unmodifiableList(), Stream.toList()를 사용하면 랩핑한 객체를 반환하기에 위 문제가 발생한다.
     */
    public List<SseEmitter> getEmitters() {
        return emitters.values().stream().toList();
    }

    public Optional<SseEmitter> getEmitter(String token) {
        SseEmitter emitter = emitters.get(token);
        return Optional.ofNullable(emitter);
    }

    public List<String> getUserTokens() {
        return getUserTokensWithinGracePeriod();
    }

    /**
     * Grace Period 내에 활성화된 적이 있는 사용자 토큰 목록을 반환합니다.
     * 현재 연결된 사용자와 최근 GRACE_PERIOD 이내에 연결이 끊긴 사용자를 포함합니다.
     */
    private List<String> getUserTokensWithinGracePeriod() {
        LocalDateTime gracePeriodThreshold = LocalDateTime.now().minus(GRACE_PERIOD);

        return lastActiveTimes.entrySet().stream()
            .filter(entry -> entry.getValue().isAfter(gracePeriodThreshold))
            .map(Map.Entry::getKey)
            .toList();
    }

    /**
     * Grace Period를 초과한 오래된 lastActiveTimes 엔트리를 정리합니다.
     * 주기적으로 호출되어 메모리 누수를 방지합니다.
     */
    public void cleanupExpiredActiveTimes() {
        LocalDateTime cleanupThreshold = LocalDateTime.now().minus(GRACE_PERIOD);

        lastActiveTimes.entrySet().removeIf(entry -> {
            boolean shouldRemove = entry.getValue().isBefore(cleanupThreshold);
            if (shouldRemove) {
                log.debug("[SSE] Grace Period 초과로 토큰 정리: {}", entry.getKey());
            }
            return shouldRemove;
        });
    }

    public int getActiveConnectionCount() {
        return emitters.size();
    }
}
