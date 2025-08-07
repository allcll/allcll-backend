package kr.allcll.backend.session;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kr.allcll.backend.session.dto.CredentialResponse;
import kr.allcll.backend.session.dto.SessionStatusResponse;
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

    private final Map<String, String> userTaskMap = new ConcurrentHashMap<>();

    private final Credentials credentials;
    private final CrawlerScheduledTaskHandler threadPoolTaskScheduler;
    private final SessionClient sessionClient;

    public void setCredential(SetCredentialRequest request) {
        Credential credential = request.toCredential();
        credentials.addCredential(credential);
    }

    public CredentialResponse getCredential(String userId) {
        Credential credential = credentials.findByUserId(userId);
        return CredentialResponse.fromCredential(credential);
    }

    public void startSession(String userId) {
        if (userTaskMap.containsKey(userId) && threadPoolTaskScheduler.isRunning(userTaskMap.get(userId))) {
            log.info("이미 해당 인증 정보로 세션 갱신 중입니다: {}", userId);
            return;
        }
        Credential credential = credentials.findByUserId(userId);
        Runnable resetSessionTask = () -> sessionClient.execute(credential, new EmptyPayload());

        String taskId = threadPoolTaskScheduler.scheduleAtFixedRate(resetSessionTask, Duration.ofSeconds(10));
        userTaskMap.put(userId, taskId);
    }

    public SessionStatusResponse getSessionStatus(String userId) {
        String taskId = userTaskMap.get(userId);

        boolean isActive = false;
        if (taskId != null) {
            isActive = threadPoolTaskScheduler.isRunning(taskId);
        }

        return SessionStatusResponse.of(isActive);
    }

    public void cancelSessionScheduling() {
        credentials.deleteAll();
        threadPoolTaskScheduler.cancelAll();
        userTaskMap.clear();
    }
}
