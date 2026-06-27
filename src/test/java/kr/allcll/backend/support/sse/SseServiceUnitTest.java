package kr.allcll.backend.support.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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
    @DisplayName("같은 SseEmitter에 대한 전송이 밀리면 최신 이벤트만 남긴다.")
    void coalescePendingEventsForSameEmitter() {
        // given
        BlockingSseEmitter slowEmitter = new BlockingSseEmitter();
        sseEmitterStorage.add("token", slowEmitter);

        // when
        sseService.propagate("token", "message", "first");
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(slowEmitter.started.getCount()).isZero());
        sseService.propagate("token", "message", "second");
        sseService.propagate("token", "message", "third");

        // then
        assertThat(slowEmitter.maxConcurrent.get()).isEqualTo(1);

        slowEmitter.release();
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(slowEmitter.sendCount.get()).isEqualTo(2));
        assertThat(slowEmitter.maxConcurrent.get()).isEqualTo(1);
        assertThat(slowEmitter.sentEvents)
            .hasSize(2)
            .anySatisfy(event -> assertThat(event).contains("event:message", "data:\"first\""))
            .anySatisfy(event -> assertThat(event).contains("event:message", "data:\"third\""))
            .noneSatisfy(event -> assertThat(event).contains("event:message", "data:\"second\""));
    }

    @Test
    @DisplayName("같은 SseEmitter에 대한 전송이 밀려도 이벤트 종류별 최신 이벤트를 보존한다.")
    void coalescePendingEventsByEventName() {
        // given
        BlockingSseEmitter slowEmitter = new BlockingSseEmitter();
        sseEmitterStorage.add("token", slowEmitter);

        // when
        sseService.propagate("token", "message", "first");
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(slowEmitter.started.getCount()).isZero());
        sseService.propagate("token", "pinSeats", "old-pin");
        sseService.propagate("token", "nonMajorSeats", "general");
        sseService.propagate("token", "pinSeats", "latest-pin");

        // then
        slowEmitter.release();
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(slowEmitter.sendCount.get()).isEqualTo(3));
        assertThat(slowEmitter.sentEvents)
            .hasSize(3)
            .anySatisfy(event -> assertThat(event).contains("event:message", "data:\"first\""))
            .anySatisfy(event -> assertThat(event).contains("event:pinSeats", "data:\"latest-pin\""))
            .anySatisfy(event -> assertThat(event).contains("event:nonMajorSeats", "data:\"general\""))
            .noneSatisfy(event -> assertThat(event).contains("event:pinSeats", "data:\"old-pin\""));
    }

    private static class BlockingSseEmitter extends SseEmitter {

        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private final AtomicInteger sendCount = new AtomicInteger();
        private final AtomicInteger concurrent = new AtomicInteger();
        private final AtomicInteger maxConcurrent = new AtomicInteger();
        private final List<String> sentEvents = new CopyOnWriteArrayList<>();

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
                sentEvents.add(eventToString(builder));
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

        private String eventToString(SseEventBuilder eventBuilder) {
            return eventBuilder.build().stream()
                .map(DataWithMediaType::getData)
                .map(Object::toString)
                .collect(Collectors.joining(""));
        }
    }
}
