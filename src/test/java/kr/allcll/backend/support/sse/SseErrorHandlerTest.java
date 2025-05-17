package kr.allcll.backend.support.sse;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SseErrorHandlerTest {

    @DisplayName("ClientAbortException이 원인인지 확인한다.")
    @Test
    void clientAbortExceptionHandleTest() {
        // given
        IOException exception = new IOException(new ClientAbortException());

        // when, then
        assertThatCode(() -> SseErrorHandler.handle(exception))
            .doesNotThrowAnyException();
    }

    @DisplayName("ClientAbortException이 원인이 아님을 확인한다.")
    @Test
    void notClientAbortExceptionHandleTest() {
        // given
        IOException exception = new IOException(new ArithmeticException());

        // when, then
        assertThatThrownBy(() -> SseErrorHandler.handle(exception))
            .isInstanceOf(RuntimeException.class);
    }
}
