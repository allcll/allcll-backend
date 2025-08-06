package kr.allcll.backend.session;

import java.time.Duration;
import kr.allcll.backend.session.dto.CredentialResponse;
import kr.allcll.backend.session.dto.SetCredentialRequest;
import kr.allcll.crawler.client.SessionClient;
import kr.allcll.crawler.client.payload.EmptyPayload;
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
        Credential credential = request.toCredential();
        credentials.addCredential(credential);
    }

    public CredentialResponse getCredential(String userId) {
        Credential credential = credentials.findByUserId(userId);
        log.info(credential.getUserId());
        return CredentialResponse.fromCredential(credential);
    }

    public void startSession(String userId) {
        Credential credential = credentials.findByUserId(userId);
        Runnable resetSessionTask = () -> sessionClient.execute(credential, new EmptyPayload());
        threadPoolTaskScheduler.scheduleAtFixedRate(resetSessionTask, Duration.ofSeconds(10));
    }

    public void cancelSessionScheduling() {
        credentials.deleteAll();
        threadPoolTaskScheduler.cancelAll();
    }
}
