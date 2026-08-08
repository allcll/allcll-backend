package kr.allcll.backend.admin.session.sso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.crawler.credential.Credential;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 픽스처의 데이터셋·필드 이름은 실서버에 대해 동작이 확인된 PoC(chemistryx/sjpt-rest-client-poc) 의 RunContext.from() 에서 그대로 가져왔다. 이 테스트는 어느 필드가 어느 토큰이
 * 되는지의 매핑만 검증하며, 필드 이름 자체가 실제 응답과 다를 가능성은 실계정 응답 캡처로만 확인할 수 있다.
 */
class SjptUserInfoResponseTest {

    private static final String USER_ID = "22011794";
    private static final String RUNNING_SEJONG = "6d760f74-d762-4f3d-b902-03dfe9e3ada2";
    private static final String LOGIN_DT = "20260722165501";

    private static final String RESPONSE = """
        {
          "dm_UserInfo": {
            "INTG_USR_NO": "%s",
            "RUNNING_SEJONG": "%s"
          },
          "dm_UserInfoGam": {
            "LOGIN_DT": "%s"
          },
          "dm_UserInfoSch": {
            "ORGN_CLSF_CD": "SCH"
          }
        }
        """.formatted(USER_ID, RUNNING_SEJONG, LOGIN_DT);

    @Test
    @DisplayName("사용자 정보 응답에서 토큰 세 개를 각각의 데이터셋에서 추출한다.")
    void parseTokensFromResponse() {
        // when
        SjptUserInfoResponse response = SjptUserInfoResponse.from(RESPONSE);

        // then
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.runningSejong()).isEqualTo(RUNNING_SEJONG);
        assertThat(response.loginDt()).isEqualTo(LOGIN_DT);
    }

    @Test
    @DisplayName("인증 정보로 변환할 때 runningSejong 과 loginDt 가 자리를 바꾸지 않는다.")
    void convertToCredentialWithoutTransposingTokens() {
        // given
        SjptUserInfoResponse response = SjptUserInfoResponse.from(RESPONSE);

        // when
        Credential credential = response.toCredential("JSESSIONID-VALUE");

        // then
        assertThat(credential.getTokenJ()).isEqualTo("JSESSIONID-VALUE");
        assertThat(credential.getTokenU()).isEqualTo(USER_ID);
        assertThat(credential.getTokenR()).isEqualTo(RUNNING_SEJONG);
        assertThat(credential.getTokenL()).isEqualTo(LOGIN_DT);
    }

    @Test
    @DisplayName("필요한 데이터셋이 없으면 파싱 실패로 처리한다.")
    void rejectResponseWithoutRequiredDataSet() {
        // given
        String withoutGam = """
            {
              "dm_UserInfo": {
                "INTG_USR_NO": "22011794",
                "RUNNING_SEJONG": "running-sejong"
              }
            }
            """;

        // when & then
        assertThatThrownBy(() -> SjptUserInfoResponse.from(withoutGam))
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.SSO_USER_INFO_PARSE_FAIL.getMessage());
    }

    @Test
    @DisplayName("필드가 빈 문자열이면 파싱 실패로 처리한다.")
    void rejectResponseWithBlankField() {
        // given
        String blankUserId = """
            {
              "dm_UserInfo": {
                "INTG_USR_NO": "",
                "RUNNING_SEJONG": "running-sejong"
              },
              "dm_UserInfoGam": {
                "LOGIN_DT": "20260722165501"
              }
            }
            """;

        // when & then
        assertThatThrownBy(() -> SjptUserInfoResponse.from(blankUserId))
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.SSO_USER_INFO_PARSE_FAIL.getMessage());
    }

    @Test
    @DisplayName("JSON 이 아닌 응답이면 파싱 실패로 처리한다.")
    void rejectNonJsonResponse() {
        // when & then
        assertThatThrownBy(() -> SjptUserInfoResponse.from("<html>login page</html>"))
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.SSO_USER_INFO_PARSE_FAIL.getMessage());
    }
}
