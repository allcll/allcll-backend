package kr.allcll.backend.admin.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import kr.allcll.backend.admin.session.sso.SjptSsoClient;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.crawler.client.SessionClient;
import kr.allcll.crawler.common.schedule.CrawlerScheduledTaskHandler;
import kr.allcll.crawler.credential.Credential;
import kr.allcll.crawler.credential.Credentials;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SessionServiceSsoTest {

    private static final String STUDENT_ID = "21011138";
    private static final String PASSWORD = "pw";

    private final Credentials credentials = mock(Credentials.class);
    private final CrawlerScheduledTaskHandler threadPoolTaskScheduler = mock(CrawlerScheduledTaskHandler.class);
    private final SessionClient sessionClient = mock(SessionClient.class);
    private final SjptSsoClient sjptSsoClient = mock(SjptSsoClient.class);
    private final SessionService sessionService = new SessionService(
        credentials, threadPoolTaskScheduler, sessionClient, sjptSsoClient
    );

    @Test
    @DisplayName("세션을 수립하면 인증 정보를 저장하고 사용자 식별자를 돌려준다.")
    void registerCredentialFromEstablishedSession() {
        // given
        Credential credential = Credential.of("tokenJ", "21011138", "tokenR", "tokenL");
        when(sjptSsoClient.establishSession(STUDENT_ID, PASSWORD)).thenReturn(credential);

        // when
        String userId = sessionService.registerBySso(STUDENT_ID, PASSWORD);

        // then
        assertThat(userId).isEqualTo("21011138");
        verify(credentials).addCredential(credential);
    }

    @Test
    @DisplayName("등록이 진행 중이면 겹치는 요청을 기다리지 않고 거절한다.")
    void rejectOverlappingRegistration() throws Exception {
        // given
        // 여석 권한을 강제 로그인으로 등록하므로, 두 번째 등록은 방금 만든 세션을 밀어낸다.
        // 대기시켜 순서대로 실행하면 그 등록이 실제로 일어나므로 즉시 거절해야 한다.
        CountDownLatch established = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(sjptSsoClient.establishSession(anyString(), anyString())).thenAnswer(invocation -> {
            established.countDown();
            release.await(5, TimeUnit.SECONDS);
            return Credential.of("tokenJ", "21011138", "tokenR", "tokenL");
        });

        AtomicReference<Exception> failure = new AtomicReference<>();
        Thread first = new Thread(() -> sessionService.registerBySso(STUDENT_ID, PASSWORD));
        first.start();
        assertThat(established.await(5, TimeUnit.SECONDS)).isTrue();

        // when
        try {
            sessionService.registerBySso(STUDENT_ID, PASSWORD);
        } catch (Exception exception) {
            failure.set(exception);
        }
        release.countDown();
        first.join(5_000);

        // then
        assertThat(failure.get())
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.SSO_REGISTRATION_IN_PROGRESS.getMessage());
        verify(sjptSsoClient, times(1)).establishSession(anyString(), anyString());
    }

    @Test
    @DisplayName("세션 수립에 실패해도 다음 등록 시도를 막지 않는다.")
    void releaseGuardOnFailure() {
        // given
        when(sjptSsoClient.establishSession(anyString(), anyString()))
            .thenThrow(new AllcllException(AllcllErrorCode.SSO_SESSION_ESTABLISH_FAIL))
            .thenReturn(Credential.of("tokenJ", "21011138", "tokenR", "tokenL"));

        // when
        assertThatThrownBy(() -> sessionService.registerBySso(STUDENT_ID, PASSWORD))
            .isInstanceOf(AllcllException.class);
        String userId = sessionService.registerBySso(STUDENT_ID, PASSWORD);

        // then
        assertThat(userId).isEqualTo("21011138");
        verify(sjptSsoClient, times(2)).establishSession(anyString(), anyString());
    }

    @Test
    @DisplayName("세션 수립에 실패하면 인증 정보를 저장하지 않는다.")
    void doNotStoreCredentialOnFailure() {
        // given
        when(sjptSsoClient.establishSession(anyString(), anyString()))
            .thenThrow(new AllcllException(AllcllErrorCode.SSO_SESSION_ESTABLISH_FAIL));

        // when
        assertThatThrownBy(() -> sessionService.registerBySso(STUDENT_ID, PASSWORD))
            .isInstanceOf(AllcllException.class);

        // then
        verify(credentials, never()).addCredential(any(Credential.class));
    }
}
