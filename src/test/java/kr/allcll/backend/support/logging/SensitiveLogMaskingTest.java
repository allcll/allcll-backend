package kr.allcll.backend.support.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCertResolver;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCertService;
import kr.allcll.backend.domain.seat.PinSeatSender;
import kr.allcll.backend.domain.seat.SeatService;
import kr.allcll.backend.domain.user.AuthFacade;
import kr.allcll.backend.domain.user.AuthService;
import kr.allcll.backend.domain.user.ToscAuthService;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserFetcher;
import kr.allcll.backend.domain.user.UserService;
import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.domain.user.dto.UserInfo;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.exception.GlobalExceptionHandler;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import kr.allcll.backend.support.scheduler.ScheduledTaskHandler;
import kr.allcll.backend.support.sse.SseEmitterStorage;
import kr.allcll.backend.support.sse.SseService;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SensitiveLogMaskingTest {

    private final List<LoggerCapture> loggerCaptures = new ArrayList<>();

    @AfterEach
    void tearDown() {
        loggerCaptures.forEach(LoggerCapture::close);
    }

    @Test
    @DisplayName("예외 로그에 요청 본문, query string, raw URI를 남기지 않는다")
    void globalExceptionHandlerDoesNotLogRequestSensitiveValues() {
        // given
        String studentId = "24000001";
        String password = "password-secret";
        String token = "query-token-secret";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin/graduation/" + studentId);
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/admin/graduation/{studentId}");
        request.setQueryString("token=" + token);
        request.setContent(("{\"studentId\":\"" + studentId + "\",\"password\":\"" + password + "\"}").getBytes());
        LoggerCapture capture = capture(GlobalExceptionHandler.class);

        // when
        new GlobalExceptionHandler().handleAllcllException(
            request,
            new AllcllException(AllcllErrorCode.INVALID_REQUEST_VALUE)
        );

        // then
        assertThat(capture.messages())
            .contains("method=POST", "route=/api/admin/graduation/{studentId}", "status=400",
                "errorCode=INVALID_REQUEST_VALUE");
        assertThat(capture.messages()).doesNotContain(studentId, password, token);
    }

    @Test
    @DisplayName("SSE 로그에 token과 token 목록을 남기지 않는다")
    void sseComponentsDoNotLogTokens() {
        // given
        String token = "sse-token-secret";
        SseEmitterStorage storage = new SseEmitterStorage();
        CallbackSseEmitter emitter = new CallbackSseEmitter();
        LoggerCapture storageCapture = capture(SseEmitterStorage.class);
        LoggerCapture serviceCapture = capture(SseService.class);
        storage.add(token, emitter);
        SseService sseService = new SseService(storage, new SeatPipelineMetrics(new SimpleMeterRegistry()));

        // when
        emitter.triggerError(new IllegalStateException(token));
        sseService.propagate(token, "pinSeats", "data");

        // then
        assertThat(storageCapture.messages()).doesNotContain(token);
        assertThat(serviceCapture.messages()).contains("eventName=pinSeats").doesNotContain(token);
    }

    @Test
    @DisplayName("핀 여석 전송 로그에 token 목록을 남기지 않는다")
    void pinSeatSenderDoesNotLogTokens() {
        // given
        String token = "pin-token-secret";
        SseService sseService = mock(SseService.class);
        SeatService seatService = mock(SeatService.class);
        ScheduledTaskHandler taskHandler = mock(ScheduledTaskHandler.class);
        PinSeatSender pinSeatSender = new PinSeatSender(sseService, seatService, taskHandler);
        LoggerCapture capture = capture(PinSeatSender.class);
        ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(taskHandler.getTaskCount()).thenReturn(0);
        when(sseService.getConnectedTokens()).thenReturn(List.of(token));
        when(seatService.getPinSeatsByTokens(List.of(token))).thenReturn(Map.of(token, List.of()));

        // when
        pinSeatSender.send();
        verify(taskHandler).scheduleAtFixedRate(taskCaptor.capture(), eq(Duration.ofSeconds(1)));
        taskCaptor.getValue().run();

        // then
        assertThat(capture.messages()).doesNotContain(token);
    }

    @Test
    @DisplayName("인증 후속 처리 실패 로그에 학번, userId, 외부 예외 원문을 남기지 않는다")
    void authFacadeDoesNotLogAuthenticationSensitiveValues() {
        // given
        String studentId = "24000002";
        String password = "login-password-secret";
        String externalMessage = "external-auth-response-secret";
        long userId = 314159L;
        LoginRequest loginRequest = new LoginRequest(studentId, password);
        AuthService authService = mock(AuthService.class);
        UserFetcher userFetcher = mock(UserFetcher.class);
        UserService userService = mock(UserService.class);
        ToscAuthService toscAuthService = mock(ToscAuthService.class);
        GraduationCertService graduationCertService = mock(GraduationCertService.class);
        GraduationCertResolver graduationCertResolver = mock(GraduationCertResolver.class);
        AuthFacade authFacade = new AuthFacade(
            authService,
            userFetcher,
            userService,
            toscAuthService,
            graduationCertService,
            graduationCertResolver
        );
        User user = mock(User.class);
        LoggerCapture capture = capture(AuthFacade.class);
        when(authService.login(loginRequest)).thenReturn(new OkHttpClient());
        doThrow(new IllegalStateException(externalMessage)).when(toscAuthService).loginTosc(eq(loginRequest));
        when(userFetcher.fetch(any())).thenReturn(UserInfo.of(studentId, "name", "department"));
        when(userService.findOrCreate(any())).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(graduationCertResolver.resolve(eq(user), any())).thenThrow(new IllegalStateException(externalMessage));

        // when
        authFacade.login(loginRequest);

        // then
        assertThat(capture.messages()).doesNotContain(studentId, password, String.valueOf(userId), externalMessage);
    }

    private LoggerCapture capture(Class<?> type) {
        Logger logger = (Logger) LoggerFactory.getLogger(type);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        LoggerCapture capture = new LoggerCapture(logger, logger.getLevel(), appender);
        logger.setLevel(Level.DEBUG);
        logger.addAppender(appender);
        loggerCaptures.add(capture);
        return capture;
    }

    private record LoggerCapture(Logger logger, Level originalLevel, ListAppender<ILoggingEvent> appender) {

        String messages() {
            return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
        }

        void close() {
            logger.detachAppender(appender);
            logger.setLevel(originalLevel);
            appender.stop();
        }
    }

    private static class CallbackSseEmitter extends SseEmitter {

        private java.util.function.Consumer<Throwable> errorCallback;

        @Override
        public void onError(java.util.function.Consumer<Throwable> callback) {
            errorCallback = callback;
        }

        void triggerError(Throwable throwable) {
            errorCallback.accept(throwable);
        }
    }
}
