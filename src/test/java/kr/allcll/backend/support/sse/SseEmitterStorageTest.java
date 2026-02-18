package kr.allcll.backend.support.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterStorageTest {

    private static final String TOKEN = "token";

    @DisplayName("재연결 시 이전 emitter의 completion 콜백이 호출되어도 최신 emitter는 유지된다.")
    @Test
    void latestConnectionShouldRemainWhenPreviousCompletionCallbackRuns() {
        SseEmitterStorage storage = new SseEmitterStorage();
        TrackableSseEmitter previousEmitter = new TrackableSseEmitter();
        TrackableSseEmitter latestEmitter = new TrackableSseEmitter();

        storage.add(TOKEN, previousEmitter);
        storage.add(TOKEN, latestEmitter);

        previousEmitter.runCompletionCallback();

        assertThat(storage.getEmitter(TOKEN)).contains(latestEmitter);
        assertThat(previousEmitter.completeCalled).isTrue();
    }

    @DisplayName("재연결 시 이전 emitter의 timeout 콜백이 호출되어도 최신 emitter는 유지된다.")
    @Test
    void latestConnectionShouldRemainWhenPreviousTimeoutCallbackRuns() {
        SseEmitterStorage storage = new SseEmitterStorage();
        TrackableSseEmitter previousEmitter = new TrackableSseEmitter();
        TrackableSseEmitter latestEmitter = new TrackableSseEmitter();

        storage.add(TOKEN, previousEmitter);
        storage.add(TOKEN, latestEmitter);

        previousEmitter.runTimeoutCallback();

        assertThat(storage.getEmitter(TOKEN)).contains(latestEmitter);
    }

    @DisplayName("현재 emitter의 error 콜백이 호출되면 연결이 제거된다.")
    @Test
    void currentEmitterShouldBeRemovedWhenErrorCallbackRuns() {
        SseEmitterStorage storage = new SseEmitterStorage();
        TrackableSseEmitter emitter = new TrackableSseEmitter();

        storage.add(TOKEN, emitter);
        emitter.runErrorCallback(new IOException("broken pipe"));

        assertThat(storage.getEmitter(TOKEN)).isEmpty();
    }

    private static class TrackableSseEmitter extends SseEmitter {

        private boolean completeCalled;
        private Runnable timeoutCallback;
        private Runnable completionCallback;
        private Consumer<Throwable> errorCallback;

        @Override
        public void complete() {
            completeCalled = true;
            super.complete();
        }

        @Override
        public void onTimeout(Runnable callback) {
            this.timeoutCallback = callback;
            super.onTimeout(callback);
        }

        @Override
        public void onCompletion(Runnable callback) {
            this.completionCallback = callback;
            super.onCompletion(callback);
        }

        @Override
        public void onError(Consumer<Throwable> callback) {
            this.errorCallback = callback;
            super.onError(callback);
        }

        private void runTimeoutCallback() {
            if (timeoutCallback != null) {
                timeoutCallback.run();
            }
        }

        private void runCompletionCallback() {
            if (completionCallback != null) {
                completionCallback.run();
            }
        }

        private void runErrorCallback(Throwable throwable) {
            if (errorCallback != null) {
                errorCallback.accept(throwable);
            }
        }
    }
}
