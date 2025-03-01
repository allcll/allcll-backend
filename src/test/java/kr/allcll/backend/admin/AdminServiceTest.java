package kr.allcll.backend.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.allcll.backend.config.AdminConfigStorage;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminConfigStorage adminConfigStorage;

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
}
