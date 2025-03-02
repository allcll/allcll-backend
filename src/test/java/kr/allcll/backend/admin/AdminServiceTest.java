package kr.allcll.backend.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import kr.allcll.backend.config.AdminConfigStorage;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.exception.AllcllSseException;
import kr.allcll.backend.support.sse.SseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminConfigStorage adminConfigStorage;

    @Autowired
    private ThreadPoolTaskScheduler scheduler;

    @Autowired
    private SseService sseService;

    @AfterEach
    void setUp() {
        adminConfigStorage.connectionClose();
    }

    @Nested
    @DisplayName("SSE 연결 가능 상태로 변경한다.")
    class sseConnectTest {

        @Test
        @DisplayName("연결 불가능 상태였을 경우 변경을 성공한다.")
        void sseConnect() {
            // when
            adminService.sseConnect();

            // then
            assertThat(adminConfigStorage.sseAccessible()).isTrue();
        }

        @Test
        @DisplayName("이미 연결 가능 상태였을 경우 예외가 발생한다.")
        void sseConnectException() {
            // given
            adminConfigStorage.connectionOpen();

            // when && then
            assertThatThrownBy(() -> adminService.sseConnect())
                .isInstanceOf(AllcllException.class)
                .hasMessage(AllcllErrorCode.SSE_CONNECTION_ALREADY_OPEN.getMessage());
        }
    }

    @Nested
    @DisplayName("SSE 연결 불가능 상태로 변경한다.")
    class sseDisconnectTest {

        @Test
        @DisplayName("연결 가능 상태였을 경우 변경을 성공한다.")
        void sseDisconnect() {
            // given
            adminConfigStorage.connectionOpen();

            // when
            adminService.sseDisconnect();

            // then
            assertThat(adminConfigStorage.sseNotAccessible()).isTrue();
        }

        @Test
        @DisplayName("이미 연결 불가능 상태였을 경우 예외가 발생한다.")
        void sseDisconnectException() {
            // when && then
            assertThatThrownBy(() -> adminService.sseDisconnect())
                .isInstanceOf(AllcllException.class)
                .hasMessage(AllcllErrorCode.SSE_CONNECTION_ALREADY_CLOSED.getMessage());
        }
    }

    @Nested
    @DisplayName("비전공 과목 전송 관련 sse 연결 테스트")
    class nonMajorWithSse {

        private ScheduledFuture<?> scheduledFuture;

        @Test
        @DisplayName("스케줄러를 통해 sse로 데이터 전송 중 연결 해제 되었을 때 스케줄러가 돌지 않는다.")
        void disconnectAfterSsePropagate() throws InterruptedException {
            // given
            int taskDuration = 201;
            adminConfigStorage.connectionOpen();
            sseService.connect("token");
            AtomicInteger executeCount = new AtomicInteger(0);
            AtomicInteger countAtCancel = new AtomicInteger(0);
            Runnable task = () -> {
                try {
                    sseService.propagate("message", "Is SSE there?");
                    executeCount.incrementAndGet();
                } catch (AllcllSseException e) {
                    countAtCancel.set(executeCount.get());
                    scheduledFuture.cancel(true);
                }
            };

            // when
            scheduledFuture = scheduler.scheduleAtFixedRate(task, Duration.ofMillis(taskDuration));
            Thread.sleep(taskDuration * 3);
            adminConfigStorage.connectionClose();

            // then
            Thread.sleep(taskDuration * 2);
            int countAfterDuration = executeCount.get();
            assertThat(countAtCancel.get()).isEqualTo(countAfterDuration);
        }
    }
}
