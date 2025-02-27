package kr.allcll.backend.support.sse;

import java.io.IOException;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
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
    private final SseAccessStorage sseAccessStorage;

    public SseEmitter connect(String token) {
        if (sseAccessStorage.isNotAccessible()) {
            throw new AllcllException(AllcllErrorCode.SSE_CONNECTION_DENIED);
        }
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
        if (sseAccessStorage.isNotAccessible()) {
            throw new AllcllException(AllcllErrorCode.SSE_CONNECTION_DENIED);
        }
        sseEmitterStorage.getEmitters().forEach(emitter -> {
            SseEventBuilder eventBuilder = SseEventBuilderFactory.create(eventName, data);
            sendEvent(emitter, eventBuilder);
        });
    }

    public void propagate(String token, String eventName, Object data) {
        if (sseAccessStorage.isNotAccessible()) {
            throw new AllcllException(AllcllErrorCode.SSE_CONNECTION_DENIED);
        }
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
