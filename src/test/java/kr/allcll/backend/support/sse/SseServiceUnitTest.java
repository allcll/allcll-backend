package kr.allcll.backend.support.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseServiceUnitTest {

    private final SseEmitterStorage sseEmitterStorage = new SseEmitterStorage();
    private final SseService sseService = new SseService(sseEmitterStorage);

    @AfterEach
    void tearDown() {
        sseService.shutdown();
    }

    @Test
    @DisplayName("SSE 브로드캐스트 전송을 비동기로 위임한다.")
    void propagateDoesNotBlockOnSlowEmitter() {
        // given
        BlockingSseEmitter slowEmitter = new BlockingSseEmitter();
        sseEmitterStorage.add("token", slowEmitter);

        // when
        long start = System.nanoTime();
        sseService.propagate("message", "slow");
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        // then
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(slowEmitter.started.getCount()).isZero());
        assertThat(elapsedMillis).isLessThan(200);

        slowEmitter.release();
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(slowEmitter.sendCount.get()).isEqualTo(1));
    }

    @Test
    @DisplayName("같은 SseEmitter에 대한 전송은 동시에 실행하지 않는다.")
    void sendSameEmitterSerially() {
        // given
        BlockingSseEmitter slowEmitter = new BlockingSseEmitter();
        sseEmitterStorage.add("token", slowEmitter);

        // when
        sseService.propagate("token", "first", "first");
        sseService.propagate("token", "second", "second");

        // then
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(slowEmitter.started.getCount()).isZero());
        assertThat(slowEmitter.maxConcurrent.get()).isEqualTo(1);

        slowEmitter.release();
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(slowEmitter.sendCount.get()).isEqualTo(2));
        assertThat(slowEmitter.maxConcurrent.get()).isEqualTo(1);
    }

    private static class BlockingSseEmitter extends SseEmitter {

        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private final AtomicInteger sendCount = new AtomicInteger();
        private final AtomicInteger concurrent = new AtomicInteger();
        private final AtomicInteger maxConcurrent = new AtomicInteger();

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            int current = concurrent.incrementAndGet();
            maxConcurrent.updateAndGet(previous -> Math.max(previous, current));
            started.countDown();
            try {
                if (!release.await(1, TimeUnit.SECONDS)) {
                    throw new IOException("send timed out");
                }
                sendCount.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            } finally {
                concurrent.decrementAndGet();
            }
        }

        void release() {
            release.countDown();
        }
    }
}
