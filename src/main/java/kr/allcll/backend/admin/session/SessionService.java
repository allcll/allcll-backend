package kr.allcll.backend.admin.session;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import kr.allcll.backend.admin.session.dto.CredentialResponse;
import kr.allcll.backend.admin.session.dto.SessionStatusResponse;
import kr.allcll.backend.admin.session.dto.SetCredentialRequest;
import kr.allcll.backend.admin.session.dto.UserSessionStatusResponse;
import kr.allcll.backend.admin.session.dto.UserSessionsStatusResponse;
import kr.allcll.backend.admin.session.sso.SjptSsoClient;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
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
    private final SjptSsoClient sjptSsoClient;

    private final Map<String, LocalDateTime> sessionUpdatedTimes = new ConcurrentHashMap<>();

    /**
     * SSO 세션 수립이 진행 중인지 나타낸다. 여석 프로그램 권한을 강제 로그인으로 등록하므로, 두 번째 요청은 방금 만든 세션을 그대로 밀어낸다. 대기시켜 순서대로 실행하면 그 두 번째 등록이
     * 실제로 일어나므로, 겹치는 요청은 기다리지 않고 거절한다.
     */
    private final AtomicBoolean ssoRegistrationInProgress = new AtomicBoolean();

    public void setCredential(SetCredentialRequest request) {
        Credential credential = request.toCredential();
        isSessionValid(credential);
        credentials.addCredential(credential);
    }

    /**
     * 관리자 계정으로 로그인해 크롤러 인증 정보를 만들고 저장한다.
     *
     * @return 수립된 세션의 사용자 식별자. 어드민 화면이 이후 갱신 시작에 사용한다.
     */
    public String registerBySso(String studentId, String password) {
        if (!ssoRegistrationInProgress.compareAndSet(false, true)) {
            throw new AllcllException(AllcllErrorCode.SSO_REGISTRATION_IN_PROGRESS);
        }
        try {
            Credential credential = sjptSsoClient.establishSession(studentId, password);
            credentials.addCredential(credential);
            return credential.getTokenU();
        } finally {
            ssoRegistrationInProgress.set(false);
        }
    }

    public CredentialResponse getCredential(String userId) {
        Credential credential = credentials.findByUserId(userId);
        boolean isValid = isSessionValid(credential);
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
        sessionUpdatedTimes.put(userId, LocalDateTime.now());
    }

    public SessionStatusResponse getSessionStatus(String userId) {
        boolean isActive = threadPoolTaskScheduler.isRunning(userId);
        return SessionStatusResponse.of(isActive);
    }

    public UserSessionsStatusResponse getSessionsStatus(List<String> userIds) {
        List<UserSessionStatusResponse> responses = userIds.stream()
            .map(userId -> new UserSessionStatusResponse(
                userId,
                threadPoolTaskScheduler.isRunning(userId),
                sessionUpdatedTimes.get(userId)
            ))
            .toList();

        return new UserSessionsStatusResponse(responses);
    }

    public void cancelSessionScheduling() {
        threadPoolTaskScheduler.cancelAll();
        credentials.deleteAll();
        sessionUpdatedTimes.clear();
    }

    private boolean isSessionValid(Credential credential) {
        try {
            sessionClient.execute(credential, new EmptyPayload());
            return true;
        } catch (CrawlerExternalRequestFailException e) {
            log.info("세션 갱신 실패: userId={}", credential.getTokenU());
        }
        return false;
    }

}
