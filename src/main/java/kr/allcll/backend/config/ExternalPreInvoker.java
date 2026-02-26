package kr.allcll.backend.config;

import kr.allcll.backend.client.ExternalService;
import kr.allcll.backend.support.sse.SseEmitterStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalPreInvoker {

    private final ExternalService externalService;
    private final SseEmitterStorage sseEmitterStorage;

    @Scheduled(fixedDelay = 1000 * 10)
    void sendPinnedSubjectsToExternal() {
        try {
            externalService.sendWantPinSubjectIdsToCrawler();
        } catch (Exception e) {
            log.error("[ExternalPreInvoker] 핀 과목 전달 중 오류 발생", e);
            throw e;
        }
        log.info("[ExternalPreInvoker] 핀 과목 전달 완료");
    }

    @Scheduled(fixedDelay = 1000 * 60)
    void cleanupExpiredSseActiveTimes() {
        try {
            sseEmitterStorage.cleanupExpiredActiveTimes();
            log.debug("[ExternalPreInvoker] SSE 활성 시각 정리 완료");
        } catch (Exception e) {
            log.error("[ExternalPreInvoker] SSE 활성 시각 정리 중 오류 발생", e);
        }
    }
}
