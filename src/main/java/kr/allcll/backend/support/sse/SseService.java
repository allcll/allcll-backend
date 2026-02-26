package kr.allcll.backend.support.sse;

import java.util.List;
import kr.allcll.backend.support.sse.dto.SseStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final SseEmitterStorage sseEmitterStorage;

    public SseEmitter connect(String token) {
        SseEmitter sseEmitter = createSseEmitter();
        sseEmitterStorage.add(token, sseEmitter);
        SseEventBuilder initialEvent = SseEventBuilderFactory.createInitialEvent();
        sendEvent(sseEmitter, initialEvent);
        return sseEmitter;
    }

    protected SseEmitter createSseEmitter() {
        return new SseEmitter();
    }

    public void propagate(String eventName, Object data) {
        sseEmitterStorage.getEmitters().forEach(emitter -> {
            SseEventBuilder eventBuilder = SseEventBuilderFactory.create(eventName, data);
            sendEvent(emitter, eventBuilder);
        });
    }

    public void propagate(String token, String eventName, Object data) {
        sseEmitterStorage.getEmitter(token).ifPresentOrElse(
            emitter -> {
                SseEventBuilder eventBuilder = SseEventBuilderFactory.create(eventName, data);
                sendEvent(emitter, eventBuilder);
                log.debug("[SSE-propagate] 이벤트 전송 완료. token: {}, eventName: {}", token, eventName);
            },
            () -> log.warn("[SSE-propagate] 이벤트 전송 실패 - Emitter가 Map에 없음. token: {}, eventName: {}", token, eventName)
        );
    }

    private void sendEvent(SseEmitter sseEmitter, SseEventBuilder eventBuilder) {
        try {
            sseEmitter.send(eventBuilder);
        } catch (Exception e) {
            log.warn("전송 실패 - SSE 연결이 끊겼습니다.: {}", e.getMessage());
            SseErrorHandler.handle(e);
        }
    }

    public List<String> getConnectedTokens() {
        return sseEmitterStorage.getUserTokens().stream().toList();
    }

    public SseStatusResponse isConnected(String token) {
        boolean isConnected = sseEmitterStorage.getEmitter(token).isPresent();
        return SseStatusResponse.of(isConnected);
    }
}
