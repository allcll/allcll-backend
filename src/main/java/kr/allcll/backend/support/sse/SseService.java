package kr.allcll.backend.support.sse;

import java.io.IOException;
import kr.allcll.backend.config.AdminConfigStorage;
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
    private final AdminConfigStorage adminConfigStorage;

    public SseEmitter connect(String token) {
        adminConfigStorage.validateSseConnection();
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
        adminConfigStorage.validateSseConnection();
        sseEmitterStorage.getEmitters().forEach(emitter -> {
            SseEventBuilder eventBuilder = SseEventBuilderFactory.create(eventName, data);
            sendEvent(emitter, eventBuilder);
        });
    }

    public void propagate(String token, String eventName, Object data) {
        adminConfigStorage.validateSseConnection();
        sseEmitterStorage.getEmitter(token).ifPresent(emitter -> {
            SseEventBuilder eventBuilder = SseEventBuilderFactory.create(eventName, data);
            sendEvent(emitter, eventBuilder);
        });
    }

    private void sendEvent(SseEmitter sseEmitter, SseEventBuilder eventBuilder) {
        try {
            sseEmitter.send(eventBuilder);
        } catch (IOException e) {
            SseErrorHandler.handle(e);
        }
    }
}
