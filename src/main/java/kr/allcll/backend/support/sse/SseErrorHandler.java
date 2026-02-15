package kr.allcll.backend.support.sse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SseErrorHandler {

    public static void handle(Exception e) {
        if (isClientAbortException(e)) {
            log.info("클라이언트가 연결을 종료했습니다.");
            return;
        }
        log.error("SSE 전송 중 예외 발생");
//        throw new RuntimeException(e);
    }

    private static boolean isClientAbortException(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof org.apache.catalina.connector.ClientAbortException) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }
}
