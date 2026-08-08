package kr.allcll.backend.admin.session.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SsoLoginRequestTest {

    private static final String PASSWORD = "sup3r-secret-pw";

    @Test
    @DisplayName("문자열로 변환해도 비밀번호가 드러나지 않는다.")
    void hidePasswordOnToString() {
        // given
        SsoLoginRequest request = new SsoLoginRequest("21011138", PASSWORD);

        // when
        String printed = request.toString();

        // then
        assertThat(printed).doesNotContain(PASSWORD);
        assertThat(printed).contains("21011138");
    }

    @Test
    @DisplayName("학번과 비밀번호가 모두 있어야 유효한 요청이다.")
    void requireBothStudentIdAndPassword() {
        // when & then
        assertThat(new SsoLoginRequest("21011138", PASSWORD).hasCredential()).isTrue();
        assertThat(new SsoLoginRequest(null, PASSWORD).hasCredential()).isFalse();
        assertThat(new SsoLoginRequest("21011138", null).hasCredential()).isFalse();
        assertThat(new SsoLoginRequest("", PASSWORD).hasCredential()).isFalse();
        assertThat(new SsoLoginRequest("21011138", " ").hasCredential()).isFalse();
    }
}
