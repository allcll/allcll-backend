package kr.allcll.backend.support.sse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SseEmitterStorage {

    private static final Duration GRACE_PERIOD = Duration.ofSeconds(30);

    private final Map<String, SseConnection> connections;

    public SseEmitterStorage() {
        this.connections = new ConcurrentHashMap<>();
    }

    public void add(String token, SseEmitter sseEmitter) {
        SseConnection connection = SseConnection.of(sseEmitter);
        connections.put(token, connection);
        log.info("[SSE] 새로운 연결이 추가되었습니다. 현재 연결 수: {}", getActiveConnectionCount());

        sseEmitter.onTimeout(() -> {
            disconnectToken(token);
            log.debug("[SSE] 연결이 타임아웃으로 종료되었습니다. 현재 연결 수: {}", getActiveConnectionCount());
        });
        sseEmitter.onError(e -> disconnectToken(token));
        sseEmitter.onCompletion(() -> disconnectToken(token));
    }

    private void disconnectToken(String token) {
        SseConnection connection = connections.get(token);
        if (connection != null) {
            connection.disconnect();
        }
    }

    /*
        실제 객체를 전달하지 않으면 이미 완료된 SseEmitter에 대해 send() 호출이 발생한다.
        Collections.unmodifiableList(), Stream.toList()를 사용하면 랩핑한 객체를 반환하기에 위 문제가 발생한다.
     */
    public List<SseEmitter> getEmitters() {
        return connections.values().stream()
            .map(SseConnection::getEmitter)
            .filter(Objects::nonNull)
            .toList();
    }

    public Optional<SseEmitter> getEmitter(String token) {
        SseConnection connection = connections.get(token);
        if (connection == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(connection.getEmitter());
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

        return connections.entrySet().stream()
            .filter(entry -> {
                SseConnection sseConnection = entry.getValue();

                if (sseConnection.isConnected()) {
                    return true;
                }

                return !sseConnection.isExpired(gracePeriodThreshold);
            })
            .map(Map.Entry::getKey)
            .toList();
    }

    /**
     * Grace Period를 초과한 오래된 연결 엔트리를 정리합니다.
     * 주기적으로 호출되어 메모리 누수를 방지합니다.
     */
    public void cleanupExpiredActiveTimes() {
        LocalDateTime cleanupThreshold = LocalDateTime.now().minus(GRACE_PERIOD);

        connections.entrySet().removeIf(entry -> {
            boolean remove = entry.getValue().shouldCleanup(cleanupThreshold);
            if (remove) {
                log.debug("[SSE] Grace Period 초과로 토큰 정리: {}", entry.getKey());
            }
            return remove;
        });
    }

    public int getActiveConnectionCount() {
        return (int) connections.values().stream()
            .filter(SseConnection::isConnected)
            .count();
    }
}
