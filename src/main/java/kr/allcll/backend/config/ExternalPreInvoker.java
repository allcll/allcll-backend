package kr.allcll.backend.config;

import kr.allcll.backend.client.ExternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalPreInvoker {

    private final ExternalService externalService;

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

    @Scheduled(fixedDelay = 1000)
    void getTargetSubjectsFromExternal() {
        try {
            externalService.getTargetSubjectsFromCrawler();
        } catch (Exception e) {
            log.error("[ExternalPreInvoker] 과목 수신 중 오류 발생", e);
            throw e;
        }
        log.info("[ExternalPreInvoker] 과목 수신 완료");
    }
}
