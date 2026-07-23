package kr.allcll.backend.admin.session.sso;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.crawler.client.support.AddParamEncoder;

/**
 * 수강신청 시스템 요청의 addParam 값을 만든다.
 * <p>
 * 서버는 모든 요청에서 현재 세션 정보를 쿼리 파라미터 addParam 으로 받는다. 값은
 * {@code base64(urlEncode(json))} 이며, 인코딩 규약은 크롤러의 {@link AddParamEncoder} 를 그대로 재사용한다(콜론과 쉼표는 인코딩하지 않는다).
 * <p>
 * 키 순서는 실제로 동작이 확인된 PoC 의 RunContext 와 동일하게 맞춘다. 서버가 순서를 따지지는 않을 것으로 보이나, 검증된 형태에서 벗어날 이유가 없다.
 */
public record SjptRunContext(
    String userId,
    String loginDt,
    String runningSejong,
    String programKey,
    String systemKey,
    Boolean forceLogin
) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SYSTEM_KEY_SCHOOL = "SCH";

    /**
     * 사용자 정보 초기화(initUserInfo.do) 요청용. 아직 토큰을 모르는 시점이라 빈 값으로 보낸다.
     */
    public static SjptRunContext empty() {
        return new SjptRunContext("", "", "", null, null, null);
    }

    /**
     * 세션이 수립된 뒤의 일반 요청용(메뉴 로드 등).
     */
    public static SjptRunContext of(SjptUserInfoResponse userInfo) {
        return new SjptRunContext(userInfo.userId(), userInfo.loginDt(), userInfo.runningSejong(), null, null, null);
    }

    /**
     * 프로그램 role 등록(initUserRole.do) 요청용.
     *
     * @param forceLogin 강제 로그인 여부. 여석 프로그램은 중복 사용 방지가 걸려 있어 남아 있는 세션을 밀어내야 하므로 true 로 등록한다.
     */
    public SjptRunContext forProgram(String programKey, boolean forceLogin) {
        return new SjptRunContext(userId, loginDt, runningSejong, programKey, SYSTEM_KEY_SCHOOL, forceLogin);
    }

    public String toAddParam() {
        String encoded = AddParamEncoder.encode(toJson());
        return Base64.getEncoder().encodeToString(encoded.getBytes(StandardCharsets.UTF_8));
    }

    public String toJson() {
        Map<String, String> body = new LinkedHashMap<>();
        if (forceLogin != null) {
            body.put("pbForceLog", String.valueOf(forceLogin));
        }
        if (programKey != null) {
            body.put("_runPgmKey", programKey);
        }
        if (systemKey != null) {
            body.put("_runSysKey", systemKey);
        }
        body.put("_runIntgUsrNo", userId);
        body.put("_runPgLoginDt", loginDt);
        body.put("_runningSejong", runningSejong);
        try {
            return OBJECT_MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            throw new AllcllException(AllcllErrorCode.SSO_USER_INFO_PARSE_FAIL, exception);
        }
    }
}
