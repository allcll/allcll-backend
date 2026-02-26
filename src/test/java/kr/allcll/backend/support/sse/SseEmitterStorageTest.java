package kr.allcll.backend.support.sse;


import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @DisplayName("[버그 재현] 30초 이상 유지되는 정상 SSE 연결이 비활성으로 처리된다")
    @Test
    void getUserTokens_shouldNotExcludeLongLivedActiveConnections() throws Exception {
        // given - SSE 연결을 생성하고 추가
        String token = "long-lived-connection";
        SseEmitter emitter = new SseEmitter(60000L); // 60초 타임아웃
        storage.add(token, emitter);

        // when - 초기 상태 확인 (연결이 활성 상태여야 함)
        assertThat(storage.getUserTokens()).contains(token);
        assertThat(storage.getActiveConnectionCount()).isEqualTo(1);
        assertThat(storage.getEmitter(token)).isPresent();

        // lastActiveTime을 31초 전으로 설정 (리플렉션 사용)
        setLastActiveTime(token, LocalDateTime.now().minusSeconds(31));

        // then - 31초 후에도 연결이 여전히 살아있음 (emitters에 존재)
        assertThat(storage.getActiveConnectionCount()).isEqualTo(1);
        assertThat(storage.getEmitter(token)).isPresent();

        // 하지만 getUserTokens()는 해당 토큰을 반환하지 않음 (버그!)
        List<String> activeTokens = storage.getUserTokens();

        // 현재 구현으로는 이 assertion이 실패함 - 버그 재현!
        assertThat(activeTokens)
            .as("30초 이상 유지되는 정상 SSE 연결도 활성 목록에 포함되어야 함")
            .contains(token);
    }

    @DisplayName("emitters에 존재하는 연결은 항상 getUserTokens에 포함되어야 한다")
    @Test
    void getUserTokens_shouldIncludeAllActiveEmitters() throws Exception {
        // given
        String oldToken = "old-active-connection";
        String newToken = "new-active-connection";

        SseEmitter oldEmitter = new SseEmitter(60000L);
        SseEmitter newEmitter = new SseEmitter(60000L);

        storage.add(oldToken, oldEmitter);

        // oldToken의 lastActiveTime을 31초 전으로 설정
        setLastActiveTime(oldToken, LocalDateTime.now().minusSeconds(31));

        // 새로운 연결 추가
        storage.add(newToken, newEmitter);

        // when
        List<String> activeTokens = storage.getUserTokens();
        int activeCount = storage.getActiveConnectionCount();

        // then
        assertThat(activeCount).isEqualTo(2); // 두 연결 모두 살아있음

        // 하지만 getUserTokens는 최근 30초 이내 연결만 반환 (버그!)
        assertThat(activeTokens)
            .as("emitters에 존재하는 모든 연결은 getUserTokens에 포함되어야 함")
            .containsExactlyInAnyOrder(oldToken, newToken);
    }

    @DisplayName("연결 종료 후 30초 이내에는 getUserTokens에 포함되어야 한다 (Grace Period)")
    @Test
    void getUserTokens_shouldIncludeRecentlyDisconnectedTokensWithinGracePeriod() throws Exception {
        // given
        String token = "disconnected-token";
        SseEmitter emitter = new SseEmitter();
        storage.add(token, emitter);

        // when - 연결 종료 시뮬레이션 (onCompletion 콜백을 수동으로 실행)
        // 실제 HTTP 연결 없는 테스트 환경에서는 complete() 호출이 콜백을 트리거하지 않음
        removeFromEmitters(token);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(storage.getActiveConnectionCount()).isEqualTo(0);
            softly.assertThat(storage.getEmitter(token)).isEmpty();
            softly.assertThat(storage.getUserTokens()).contains(token);
        });

        setLastActiveTime(token, LocalDateTime.now().minusSeconds(31));

        // 31초 후에는 제외됨
        assertThat(storage.getUserTokens()).doesNotContain(token);
    }

    @DisplayName("cleanupExpiredActiveTimes는 Grace Period를 초과한 비활성 토큰만 제거한다")
    @Test
    void cleanupExpiredActiveTimes_shouldOnlyRemoveExpiredInactiveTokens() throws Exception {
        // given
        /// 활성 연결 추가
        String activeToken = "still-connected";
        SseEmitter activeEmitter = new SseEmitter(60000L);
        storage.add(activeToken, activeEmitter);

        /// 만료될 비활성 연결 추가
        String expiredToken = "expired-disconnected";
        SseEmitter expiredEmitter = new SseEmitter();
        storage.add(expiredToken, expiredEmitter);
        removeFromEmitters(expiredToken);
        setLastActiveTime(expiredToken, LocalDateTime.now().minusSeconds(31));  // 31초 전으로 설정

        // when
        storage.cleanupExpiredActiveTimes();

        // then
        List<String> tokens = storage.getUserTokens();
        assertThat(tokens)
            .contains(activeToken)
            .doesNotContain(expiredToken);
    }

    @SuppressWarnings("unchecked")
    private void setLastActiveTime(String token, LocalDateTime time) throws Exception {
        Field connectionsField = SseEmitterStorage.class.getDeclaredField("connections");
        connectionsField.setAccessible(true);
        Map<String, Object> connections = (Map<String, Object>) connectionsField.get(storage);

        Object connection = connections.get(token);
        if (connection != null) {
            Field emitterField = connection.getClass().getDeclaredField("emitter");
            emitterField.setAccessible(true);
            SseEmitter emitter = (SseEmitter) emitterField.get(connection);

            Class<?> sseConnectionClass = Class.forName("kr.allcll.backend.support.sse.SseConnection");
            var constructor = sseConnectionClass.getDeclaredConstructor(SseEmitter.class, LocalDateTime.class);
            constructor.setAccessible(true);
            Object newConnection = constructor.newInstance(emitter, time);
            connections.put(token, newConnection);
        }
    }

    @SuppressWarnings("unchecked")
    private void removeFromEmitters(String token) throws Exception {
        Field connectionsField = SseEmitterStorage.class.getDeclaredField("connections");
        connectionsField.setAccessible(true);
        Map<String, Object> connections = (Map<String, Object>) connectionsField.get(storage);

        Object connection = connections.get(token);
        if (connection != null) {
            var disconnectMethod = connection.getClass().getDeclaredMethod("disconnect");
            disconnectMethod.setAccessible(true);
            disconnectMethod.invoke(connection);
        }
    }
}
