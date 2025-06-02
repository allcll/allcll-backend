package kr.allcll.backend.config;

import kr.allcll.backend.client.ExternalService;
import kr.allcll.backend.support.sse.SseClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalPreInvoker {

    private final ExternalService externalService;
    private final SseClientService sseClientService;

    @Retryable(
        maxAttempts = 12 * 60 * 7,
        backoff = @Backoff(delay = 5000, multiplier = 1.5, maxDelay = 10000)
    )
    public void sendSseConnectionToExternal() {
        try {
            keepSseConnection();
        } catch (Exception e) {
            log.error("[외부 서버 통신] {}", e.getMessage(), e);
            throw e;
        }
    }

    private void keepSseConnection() {
        while (true) {
            log.info("[외부 서버 통신] SSE 연결 시도");
            sseClientService.getSseData();
            log.info("[외부 서버 통신] SSE 연결 종료");
        }
    }

    @Scheduled(fixedDelay = 1000 * 10)
    void sendPinnedSubjectsToExternal() {
        try {
            externalService.sendWantPinSubjectIdsToCrawler();
        } catch (Exception e) {
            log.error("[외부 서버 통신] 핀 과목 전달 중 오류 발생", e);
            throw e;
        }
        log.info("[외부 서버 통신] 핀 과목 전달 완료");
    }
}
