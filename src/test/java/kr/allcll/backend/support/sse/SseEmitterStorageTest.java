package kr.allcll.backend.support.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterStorageTest {

    private SseEmitterStorage storage;

    @BeforeEach
    void setUp() {
        storage = new SseEmitterStorage();
    }

    @DisplayName("SSE 연결 추가 시 getUserTokens에서 토큰이 조회된다")
    @Test
    void add_shouldRecordLastActiveTime() {
        // given
        String token = "test-token";
        SseEmitter emitter = new SseEmitter();

        // when
        storage.add(token, emitter);

        // then
        List<String> tokens = storage.getUserTokens();
        assertThat(tokens).contains(token);
    }

    @DisplayName("여러 연결이 있을 때 모두 토큰으로 조회된다")
    @Test
    void getUserTokens_shouldReturnAllTokens() {
        // given
        String token1 = "token1";
        String token2 = "token2";
        String token3 = "token3";

        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();
        SseEmitter emitter3 = new SseEmitter();

        storage.add(token1, emitter1);
        storage.add(token2, emitter2);
        storage.add(token3, emitter3);

        // when
        List<String> tokens = storage.getUserTokens();

        // then
        assertThat(tokens).containsExactlyInAnyOrder(token1, token2, token3);
    }

    @DisplayName("정리 작업 실행 시 최근 토큰은 제거되지 않는다")
    @Test
    void cleanupExpiredActiveTimes_shouldNotRemoveRecentTokens() {
        // given
        String token = "test-token";
        SseEmitter emitter = new SseEmitter();
        storage.add(token, emitter);

        // when
        storage.cleanupExpiredActiveTimes();

        // then - Grace Period 내이므로 여전히 존재
        List<String> tokens = storage.getUserTokens();
        assertThat(tokens).contains(token);
    }

    @DisplayName("활성 연결 수는 실제 emitters 수와 동일하다")
    @Test
    void getActiveConnectionCount_shouldReturnEmittersSize() {
        // given
        String token1 = "token1";
        String token2 = "token2";

        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();

        storage.add(token1, emitter1);
        storage.add(token2, emitter2);

        // when & then
        assertThat(storage.getActiveConnectionCount()).isEqualTo(2);
    }

    @DisplayName("Emitter를 정상적으로 가져올 수 있다")
    @Test
    void getEmitter_shouldReturnCorrectEmitter() {
        // given
        String token = "test-token";
        SseEmitter emitter = new SseEmitter();
        storage.add(token, emitter);

        // when & then
        assertThat(storage.getEmitter(token)).isPresent();
    }

    @DisplayName("존재하지 않는 토큰으로 조회 시 빈 Optional을 반환한다")
    @Test
    void getEmitter_shouldReturnEmptyForNonExistentToken() {
        // when & then
        assertThat(storage.getEmitter("non-existent")).isEmpty();
    }
}