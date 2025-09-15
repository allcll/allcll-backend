package kr.allcll.backend.support.sse;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
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
    private volatile SseStatus currentStatus = SseStatus.IDLE;

    public SseEmitter connect(String token) {
        SseEmitter sseEmitter = createSseEmitter();
        sseEmitterStorage.add(token, sseEmitter);
        SseEventBuilder initialEvent = SseEventBuilderFactory.createInitialEvent();
        sendEvent(sseEmitter, initialEvent);
        sendStatusEvent(sseEmitter);
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
        sseEmitterStorage.getEmitter(token).ifPresent(emitter -> {
            SseEventBuilder eventBuilder = SseEventBuilderFactory.create(eventName, data);
            sendEvent(emitter, eventBuilder);
        });
    }

    public void updateStatus(SseStatus newStatus) {
        if (Objects.equals(this.currentStatus, newStatus)) {
            return;
        }
        this.currentStatus = newStatus;
        propagateStatus();
    }

    private void sendEvent(SseEmitter sseEmitter, SseEventBuilder eventBuilder) {
        try {
            sseEmitter.send(eventBuilder);
        } catch (IOException e) {
            log.warn("전송 실패 - SSE 연결이 끊겼습니다.: {}", e.getMessage());
            SseErrorHandler.handle(e);
        }
    }

    private void sendStatusEvent(SseEmitter sseEmitter) {
        SseStatusResponse sseStatusResponse = SseStatusResponse.of(currentStatus.name().toLowerCase(),
            currentStatus.getMessage());

        SseEventBuilder eventBuilder = SseEventBuilderFactory.create("status", sseStatusResponse);
        sendEvent(sseEmitter, eventBuilder);
    }

    private void propagateStatus() {
        SseStatusResponse statusResponse = SseStatusResponse.of(currentStatus.name().toLowerCase(),
            currentStatus.getMessage());

        sseEmitterStorage.getEmitters().forEach(emitter -> {
            SseEventBuilder eventBuilder = SseEventBuilderFactory.create("status", statusResponse);
            sendEvent(emitter, eventBuilder);
        });
    }

    public List<String> getConnectedTokens() {
        return sseEmitterStorage.getUserTokens().stream().toList();
    }
}
