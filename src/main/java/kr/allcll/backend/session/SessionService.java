package kr.allcll.backend.session;

import java.time.Duration;
import kr.allcll.backend.session.dto.CredentialResponse;
import kr.allcll.backend.session.dto.SessionStatusResponse;
import kr.allcll.backend.session.dto.SetCredentialRequest;
import kr.allcll.crawler.client.SessionClient;
import kr.allcll.crawler.client.payload.EmptyPayload;
import kr.allcll.crawler.common.exception.CrawlerExternalRequestFailException;
import kr.allcll.crawler.common.schedule.CrawlerScheduledTaskHandler;
import kr.allcll.crawler.credential.Credential;
import kr.allcll.crawler.credential.Credentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final Credentials credentials;
    private final CrawlerScheduledTaskHandler threadPoolTaskScheduler;
    private final SessionClient sessionClient;

    public void setCredential(SetCredentialRequest request) {
        boolean isValidRequest = isValidCredentialRequest(request);
        if (!isValidRequest) {
            return;
        }
        Credential credential = request.toCredential();
        validateCredential(credential);
        credentials.addCredential(credential);
    }

    public CredentialResponse getCredential(String userId) {
        Credential credential = credentials.findByUserId(userId);
        boolean isValid = validateCredential(credential);
        if (isValid) {
            return CredentialResponse.fromCredential(credential);
        }
        return CredentialResponse.ofInvalidCredential(null);
    }

    public void startSession(String userId) {
        if (threadPoolTaskScheduler.isRunning(userId)) {
            log.info("이미 해당 인증 정보로 세션 갱신 중입니다: {}", userId);
            return;
        }
        Credential credential = credentials.findByUserId(userId);
        Runnable resetSessionTask = () -> {
            try {
                sessionClient.execute(credential, new EmptyPayload());
                log.info("세션 갱신 성공: userId={}", userId);
            } catch (CrawlerExternalRequestFailException e) {
                log.error("세션 갱신 실패: userId={}", userId);
                cancelSessionScheduling();
            }
        };
        threadPoolTaskScheduler.scheduleAtFixedRate(userId, resetSessionTask, Duration.ofSeconds(10));
    }

    public SessionStatusResponse getSessionStatus(String userId) {
        boolean isActive = threadPoolTaskScheduler.isRunning(userId);
        return SessionStatusResponse.of(isActive);
    }

    public void cancelSessionScheduling() {
        threadPoolTaskScheduler.cancelAll();
        credentials.deleteAll();
    }

    private boolean isValidCredentialRequest(SetCredentialRequest request) {
        if (request.tokenJ() == null || request.tokenU() == null || request.tokenR() == null
            || request.tokenL() == null) {
            return false;
        }
        return true;
    }

    private boolean validateCredential(Credential credential) {
        try {
            sessionClient.execute(credential, new EmptyPayload());
            return true;
        } catch (CrawlerExternalRequestFailException e) {
            log.info("세션 갱신 실패: userId={}", credential.getTokenU());
        }
        return false;
    }
}
