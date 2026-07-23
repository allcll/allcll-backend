package kr.allcll.backend.admin.session.sso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 응답의 데이터셋·필드 이름은 실계정으로 initUserRole.do 를 호출해 확인한 구조를 따른다. 세 프로그램(여석/관심과목/과목조회) 모두 dm_UserRole.MENU_CD 로 요청한 키를 그대로
 * 돌려주는 것을 확인했다.
 */
class SjptUserRoleResponseTest {

    private static final String SEAT_PROGRAM_KEY = "SELF_STUDSELF_SUB_30SELF_MENU_10SueReqLesnEGuide";
    private static final String BASKET_PROGRAM_KEY = "SELF_STUDSELF_SUB_30SELF_MENU_10SueReqLesnBasketGuide";

    @Test
    @DisplayName("등록한 프로그램 키를 응답에서 읽는다.")
    void readRegisteredProgramKey() {
        // given
        String response = roleResponse(SEAT_PROGRAM_KEY);

        // when
        SjptUserRoleResponse parsed = SjptUserRoleResponse.from(response);

        // then
        assertThat(parsed.programKey()).isEqualTo(SEAT_PROGRAM_KEY);
    }

    @Test
    @DisplayName("요청한 프로그램이 그대로 돌아오면 등록에 성공한 것으로 본다.")
    void acceptResponseForRequestedProgram() {
        // given
        SjptUserRoleResponse parsed = SjptUserRoleResponse.from(roleResponse(SEAT_PROGRAM_KEY));

        // when & then
        assertThatCode(() -> parsed.validateRegisteredFor(SEAT_PROGRAM_KEY)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다른 프로그램이 돌아오면 등록 실패로 처리한다.")
    void rejectResponseForDifferentProgram() {
        // given
        SjptUserRoleResponse parsed = SjptUserRoleResponse.from(roleResponse(BASKET_PROGRAM_KEY));

        // when & then
        assertThatThrownBy(() -> parsed.validateRegisteredFor(SEAT_PROGRAM_KEY))
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.SSO_PROGRAM_ROLE_REGISTER_FAIL.getMessage());
    }

    @Test
    @DisplayName("권한 정보가 없는 응답은 등록 실패로 처리한다.")
    void rejectResponseWithoutUserRole() {
        // given
        String withoutRole = "{\"dm_UserInfo\":{\"INTG_USR_NO\":\"22011794\"}}";

        // when & then
        assertThatThrownBy(() -> SjptUserRoleResponse.from(withoutRole))
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.SSO_PROGRAM_ROLE_REGISTER_FAIL.getMessage());
    }

    @Test
    @DisplayName("성공을 가장한 HTML 응답은 등록 실패로 처리한다.")
    void rejectHtmlResponse() {
        // given
        // 이 시스템은 실패해도 200 과 HTML 을 돌려주고 본문에 '실패' 같은 문자열이 섞여 있다.
        String html = "<html><body>오류가 발생했습니다</body></html>";

        // when & then
        assertThatThrownBy(() -> SjptUserRoleResponse.from(html))
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.SSO_PROGRAM_ROLE_REGISTER_FAIL.getMessage());
    }

    private String roleResponse(String programKey) {
        return """
            {
              "dm_UserRole": {
                "MENU_CD": "%s",
                "MENU_LEVEL": 4,
                "PG_LOGIN_DT": "20260722165501"
              },
              "dm_UserInfo": {
                "INTG_USR_NO": "22011794"
              }
            }
            """.formatted(programKey);
    }
}
