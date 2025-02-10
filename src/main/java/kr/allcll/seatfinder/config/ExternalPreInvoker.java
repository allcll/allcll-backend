package kr.allcll.seatfinder.config;

import kr.allcll.seatfinder.external.ExternalService;
import kr.allcll.seatfinder.sse.SseClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalPreInvoker {

    private final ExternalService externalService;
    private final SseClientService sseClientService;

    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 60 * 60 * 7,
        backoff = @Backoff(delay = 1000)
    )
    public void saveNonMajorSubjects() {
        try {
            externalService.saveNonMajorSubjects();
        } catch (Exception e) {
            log.error("교양 top20 저장 중 오류 발생", e);
            throw e;
        }
        log.info("교양 top20 저장 완료");
    }

    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 12 * 60 * 7,
        backoff = @Backoff(delay = 5000)
    )
    public void retrieveSendNonMajor() {
        try {
            externalService.sendToCrawlerNonMajorRequest();
        } catch (Exception e) {
            log.error("교양 top20 전달 중 오류 발생", e);
            throw e;
        }
        log.info("교양 top20 전달 완료");
    }

    @Retryable(
        maxAttempts = 10 * 60 * 60 * 7,
        backoff = @Backoff(delay = 100)
    )
    public void requestSseConnection() {
        try {
            sseClientService.getSseData();
        } catch (Exception e) {
            log.error("SSE: {}", e.getMessage(), e);
            throw e;
        }
    }
}
