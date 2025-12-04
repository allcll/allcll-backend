package kr.allcll.backend.support.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PrefixParserTest {

    @Test
    @DisplayName("prefix 내용을 추출한다.")
    void extractContent() {
        // given
        String taskId = "[21011138]" + TokenProvider.create();

        // when
        String userId = PrefixParser.extract(taskId);

        // then
        assertThat(userId).isEqualTo("21011138");
    }

    @Test
    @DisplayName("task ID에서 user ID가 중복을 제외하고 추출된다.")
    void extractAllWithOutDuplicate() {
        // given
        String userIdPrefixA = "[21011138]";
        String userIdPrefixB = "[20010187]";
        List<String> taskIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String id = userIdPrefixA + TokenProvider.create();
            taskIds.add(id);
        }
        for (int i = 0; i < 5; i++) {
            String id = userIdPrefixB + TokenProvider.create();
            taskIds.add(id);
        }

        // when
        List<String> allId = PrefixParser.extractAllWithOutDuplicate(taskIds);

        // then
        assertThat(allId)
            .hasSize(2)
            .isEqualTo(List.of("21011138", "20010187"));
    }

    @ParameterizedTest
    @DisplayName("유효하지 않은 prefix 형식은 전부 예외를 발생시킨다.")
    @ValueSource(strings = {
        "[21011138",
        "21011138]",
        "]21011138["
    })
    void invalidPrefix(String prefix) {
        // given
        String taskId = prefix + TokenProvider.create();

        // when & then
        assertThatThrownBy(() -> PrefixParser.extract(taskId))
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.PREFIX_NOT_FOUND.getMessage());
    }
}
