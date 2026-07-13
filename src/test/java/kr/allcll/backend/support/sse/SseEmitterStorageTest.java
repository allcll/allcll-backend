package kr.allcll.backend.support.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterStorageTest {

    private final SseEmitterStorage sseEmitterStorage = new SseEmitterStorage();

    @Test
    @DisplayName("이전 SSE 연결의 종료 콜백은 같은 토큰의 새 연결을 삭제하지 않는다.")
    void staleEmitterCallbacksDoNotRemoveNewEmitter() {
        // given
        CallbackSseEmitter oldEmitter = new CallbackSseEmitter();
        CallbackSseEmitter newEmitter = new CallbackSseEmitter();
        String token = "token";
        sseEmitterStorage.add(token, oldEmitter);
        sseEmitterStorage.add(token, newEmitter);

        // when
        oldEmitter.triggerTimeout();
        oldEmitter.triggerError();
        oldEmitter.triggerCompletion();

        // then
        assertThat(sseEmitterStorage.getEmitter(token)).contains(newEmitter);
    }

    @Test
    @DisplayName("현재 SSE 연결이 완료되면 저장소에서 삭제한다.")
    void currentEmitterCompletionRemovesEmitter() {
        // given
        CallbackSseEmitter emitter = new CallbackSseEmitter();
        String token = "token";
        sseEmitterStorage.add(token, emitter);

        // when
        emitter.triggerCompletion();

        // then
        assertThat(sseEmitterStorage.getEmitter(token)).isEmpty();
    }

    private static class CallbackSseEmitter extends SseEmitter {

        private Runnable timeoutCallback;
        private Consumer<Throwable> errorCallback;
        private Runnable completionCallback;

        @Override
        public void onTimeout(Runnable callback) {
            timeoutCallback = callback;
        }

        @Override
        public void onError(Consumer<Throwable> callback) {
            errorCallback = callback;
        }

        @Override
        public void onCompletion(Runnable callback) {
            completionCallback = callback;
        }

        void triggerTimeout() {
            timeoutCallback.run();
        }

        void triggerError() {
            errorCallback.accept(new IllegalStateException("connection error"));
        }

        void triggerCompletion() {
            completionCallback.run();
        }
    }
}
