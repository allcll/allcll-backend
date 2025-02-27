package kr.allcll.backend.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

class SseEventBuilderFactoryTest {

    @DisplayName("SSE 이벤트를 생성한다.")
    @Test
    void testCreateEvent() {
        // given
        TestHelper.TestClass testData = new TestHelper.TestClass("A", "EMPTY");
        String expected = "retry:1000\n" +
            "event:seat\n" +
            "data:{\"name\":\"A\",\"status\":\"EMPTY\"}\n\n";

        // when
        SseEventBuilder event = SseEventBuilderFactory.create("seat", testData);
        String actual = TestHelper.eventToString(event);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("SSE 초기 이벤트의 내용을 검증한다.")
    @Test
    void testCreateInitialEvent() {
        // given
        String expected = "retry:1000\n" +
            "event:connection\n" +
            "data:success\n\n";

        // when
        SseEventBuilder initialEvent = SseEventBuilderFactory.createInitialEvent();
        String actual = TestHelper.eventToString(initialEvent);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    private static class TestHelper {

        public static String eventToString(SseEventBuilder eventBuilder) {
            return eventBuilder.build().stream()
                .map(DataWithMediaType::getData)
                .map(Object::toString)
                .collect(Collectors.joining(""));
        }

        public record TestClass(String name, String status) {

        }
    }
}
