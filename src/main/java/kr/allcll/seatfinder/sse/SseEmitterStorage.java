package kr.allcll.seatfinder.sse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SseEmitterStorage {

    /*
        ExecutorService로 변경이 필요할 수 있습니다.
     */
    private final Map<String, SseEmitter> emitters;

    /*
        동시성 문제로 자료구조 변경이 필요할 수 있습니다.
     */
    public SseEmitterStorage() {
        this.emitters = new ConcurrentHashMap<>();
    }

    public void add(String token, SseEmitter sseEmitter) {
        emitters.put(token, sseEmitter);
        log.info("[SSE] 새로운 연결이 추가되었습니다. 현재 연결 수: {}", emitters.size());
        sseEmitter.onTimeout(() -> {
            emitters.remove(token);
            log.debug("[SSE] 연결이 타임아웃으로 종료되었습니다. 현재 연결 수: {}", emitters.size());
        });
        sseEmitter.onError(e -> {
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

    public List<String> getUserTokens() {
        return emitters.keySet().stream().toList();
    }
}
