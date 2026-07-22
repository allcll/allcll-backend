package kr.allcll.backend.admin.session.sso;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.crawler.credential.Credential;

/**
 * 수강신청 시스템의 initUserInfo.do 응답에서 크롤러 인증 정보에 필요한 값을 추출한다.
 * <p>
 * 응답은 데이터셋별로 나뉘어 있고, 필요한 값 셋이 서로 다른 데이터셋에 흩어져 있다.
 * <pre>
 * dm_UserInfo.INTG_USR_NO    -&gt; tokenU (_runIntgUsrNo)
 * dm_UserInfo.RUNNING_SEJONG -&gt; tokenR (_runningSejong)
 * dm_UserInfoGam.LOGIN_DT    -&gt; tokenL (_runPgLoginDt)
 * </pre>
 * tokenJ(JSESSIONID) 는 이 응답이 아니라 doSsoLogin.do 의 쿠키에서 나온다.
 */
public record SjptUserInfoResponse(
    String userId,
    String runningSejong,
    String loginDt
) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static SjptUserInfoResponse from(String responseBody) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(responseBody);
            return new SjptUserInfoResponse(
                readRequiredText(root, "dm_UserInfo", "INTG_USR_NO"),
                readRequiredText(root, "dm_UserInfo", "RUNNING_SEJONG"),
                readRequiredText(root, "dm_UserInfoGam", "LOGIN_DT")
            );
        } catch (JsonProcessingException exception) {
            throw new AllcllException(AllcllErrorCode.SSO_USER_INFO_PARSE_FAIL, exception);
        }
    }

    /**
     * Credential.of 의 인자 순서는 (jSessionId, userId, runningSejong, loginDt) 로 tokenR 과 tokenL 이 J/U/R/L 표기 순서와
     * 어긋난다. 두 값을 바꿔 넣어도 타입이 같아 컴파일 시점에 걸리지 않으므로, 변환은 이 메서드 한 곳에서만 한다.
     */
    public Credential toCredential(String jSessionId) {
        return Credential.of(jSessionId, userId, runningSejong, loginDt);
    }

    private static String readRequiredText(JsonNode root, String dataSet, String field) {
        JsonNode value = root.path(dataSet).path(field);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new AllcllException(AllcllErrorCode.SSO_USER_INFO_PARSE_FAIL);
        }
        return value.asText();
    }
}
