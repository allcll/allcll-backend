package kr.allcll.seatfinder.config;

import kr.allcll.seatfinder.external.ExternalService;
import kr.allcll.seatfinder.sse.SseClientService;
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
        retryFor = {Exception.class},
        maxAttempts = 60 * 60 * 7,
        backoff = @Backoff(delay = 100, multiplier = 1.5, maxDelay = 5000)
    )
    public void saveNonMajorSubjects() {
        try {
            externalService.saveNonMajorSubjects();
        } catch (Exception e) {
            log.error("[서버] 상위권 교양 저장 중 오류 발생", e);
            throw e;
        }
        log.info("[서버] 상위권 교양 저장 완료");
    }

    @Retryable(
        maxAttempts = 12 * 60 * 7,
        backoff = @Backoff(delay = 100, multiplier = 1.5, maxDelay = 5000)
    )
    public void sendSseConnectionToExternal() {
        sendNonMajorToExternal();
        try {
            keepSseConnection();
        } catch (Exception e) {
            log.error("[외부 서버 통신] {}", e.getMessage(), e);
            throw e;
        }
    }

    private void sendNonMajorToExternal() {
        try {
            externalService.sendNonMajorToExternal();
        } catch (Exception e) {
            log.error("[외부 서버 통신] 상위권 교양 전달 중 오류 발생", e);
            throw e;
        }
        log.info("[외부 서버 통신] 상위권 교양 전달 완료");
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
