package kr.allcll.backend.support.sse;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
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

    private static final String INITIAL_EVENT_NAME = "connection";

    private final SseEmitterStorage sseEmitterStorage;
    private final SeatPipelineMetrics seatPipelineMetrics;
    private final ExecutorService sseSendExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<SseEmitter, SseSendState> sendStates = new ConcurrentHashMap<>();

    public SseEmitter connect(String token) {
        SseEmitter sseEmitter = createSseEmitter();
        sseEmitterStorage.add(token, sseEmitter);
        sseEmitter.onTimeout(() -> sendStates.remove(sseEmitter));
        sseEmitter.onError(e -> sendStates.remove(sseEmitter));
        sseEmitter.onCompletion(() -> sendStates.remove(sseEmitter));

        SseEventBuilder initialEvent = SseEventBuilderFactory.createInitialEvent();
        sendEventAsync(sseEmitter, INITIAL_EVENT_NAME, initialEvent);

        return sseEmitter;
    }

    protected SseEmitter createSseEmitter() {
        return new SseEmitter();
    }

    public void propagate(String eventName, Object data) {
        String sseEvent = SseEventBuilderFactory.serialize(data);
        sseEmitterStorage.getEmitters().forEach(emitter -> {
            SseEventBuilder eventBuilder = SseEventBuilderFactory.createSerialized(eventName, sseEvent);
            sendEventAsync(emitter, eventName, eventBuilder);
        });
    }

    public void propagate(String token, String eventName, Object data) {
        sseEmitterStorage.getEmitter(token).ifPresentOrElse(
            emitter -> {
                SseEventBuilder eventBuilder = SseEventBuilderFactory.create(eventName, data);
                sendEventAsync(emitter, eventName, eventBuilder);
                log.debug("[SSE-propagate] 이벤트 전송 요청. token: {}, eventName: {}", token, eventName);
            },
            () -> log.warn("[SSE-propagate] 이벤트 전송 요청 실패 - Emitter가 Map에 없음. token: {}, eventName: {}", token,
                eventName)
        );
    }

    private void sendEventAsync(SseEmitter sseEmitter, String eventName, SseEventBuilder eventBuilder) {
        SseSendState sendState = sendStates.computeIfAbsent(sseEmitter, ignored -> new SseSendState());
        if (sendState.updatePending(eventName, eventBuilder)) {
            sseSendExecutor.execute(() -> drainEvents(sseEmitter, sendState));
        }
    }

    private void drainEvents(SseEmitter sseEmitter, SseSendState sendState) {
        while (true) {
            List<SseEventBuilder> eventBuilders = sendState.pollPending();
            if (eventBuilders.isEmpty()) {
                return;
            }
            eventBuilders.forEach(eventBuilder -> sendEvent(sseEmitter, eventBuilder));
        }
    }

    private void sendEvent(SseEmitter sseEmitter, SseEventBuilder eventBuilder) {
        try {
            seatPipelineMetrics.recordSseSend(() -> sseEmitter.send(eventBuilder));
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

    @PreDestroy
    void shutdown() {
        sseSendExecutor.shutdown();
    }

    private static class SseSendState {

        private boolean sending;
        private final Map<String, SseEventBuilder> pendingEvents = new LinkedHashMap<>();

        synchronized boolean updatePending(String eventName, SseEventBuilder eventBuilder) {
            pendingEvents.put(eventName, eventBuilder);
            if (sending) {
                return false;
            }
            sending = true;
            return true;
        }

        synchronized List<SseEventBuilder> pollPending() {
            if (pendingEvents.isEmpty()) {
                sending = false;
                return List.of();
            }
            List<SseEventBuilder> eventBuilders = new ArrayList<>(pendingEvents.values());
            pendingEvents.clear();
            return eventBuilders;
        }
    }
}
