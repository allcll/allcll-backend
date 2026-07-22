package kr.allcll.backend.admin.session.sso;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 오류 봉투의 형태와 ERRTYPE 값은 실계정에 잘못된 비밀번호를 넣어 확인했다.
 */
class SjptSubmitErrorTest {

    private static final String NOT_AUTHENTICATED_RESPONSE = """
        {
          "_SUBMIT_ERROR_": {
            "ERRMSG": "로그인후 지정된 시간동안 사용하지 않아 자동로그아웃 처리 되었습니다.",
            "ERRTYPE": "LOGOUT",
            "ERRCODE": "로그인후 지정된 시간동안 사용하지 않아 자동로그아웃 처리 되었습니다."
          }
        }
        """;

    @Test
    @DisplayName("인증되지 않은 요청은 로그아웃 유형의 오류로 돌아온다.")
    void detectNotAuthenticated() {
        // when
        SjptSubmitError error = SjptSubmitError.find(NOT_AUTHENTICATED_RESPONSE).orElseThrow();

        // then
        assertThat(error.errorType()).isEqualTo("LOGOUT");
        assertThat(error.isNotAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("다른 유형의 오류는 인증 실패로 보지 않는다.")
    void distinguishOtherErrorType() {
        // given
        String response = "{\"_SUBMIT_ERROR_\":{\"ERRMSG\":\"처리 중 오류\",\"ERRTYPE\":\"SYSTEM\"}}";

        // when
        SjptSubmitError error = SjptSubmitError.find(response).orElseThrow();

        // then
        assertThat(error.isNotAuthenticated()).isFalse();
        assertThat(error.message()).isEqualTo("처리 중 오류");
    }

    @Test
    @DisplayName("정상 응답에는 오류 봉투가 없다.")
    void findNothingInSuccessResponse() {
        // given
        String response = "{\"dm_UserInfo\":{\"INTG_USR_NO\":\"22011794\"}}";

        // when & then
        assertThat(SjptSubmitError.find(response)).isEmpty();
    }

    @Test
    @DisplayName("성공 응답 본문에 실패 관련 문자열이 섞여 있어도 오류로 보지 않는다.")
    void ignoreFailureWordsInSuccessResponse() {
        // given
        // 포털 로그인 성공 응답에도 '실패', 'Error' 문자열이 들어 있다(실계정 캡처로 확인).
        String response = "{\"dm_UserInfo\":{\"MESSAGE\":\"로그인 실패 시 Error 페이지로 이동\"}}";

        // when & then
        assertThat(SjptSubmitError.find(response)).isEmpty();
    }

    @Test
    @DisplayName("JSON 이 아닌 응답에서는 오류 봉투를 찾지 않는다.")
    void findNothingInHtmlResponse() {
        // when & then
        assertThat(SjptSubmitError.find("<html><body>로그인 처리</body></html>")).isEmpty();
    }
}
