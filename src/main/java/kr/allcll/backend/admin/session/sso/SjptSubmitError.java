package kr.allcll.backend.admin.session.sso;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

/**
 * 수강신청 시스템이 요청을 거절할 때 돌려주는 오류 봉투.
 * <p>
 * 이 시스템은 실패해도 HTTP 200 을 주고, 성공 응답의 본문에도 "실패", "error" 같은 문자열이 섞여 있어 상태 코드나 문자열 포함 여부로는 판정할 수 없다. 대신 실패 응답에는 아래 형태가
 * 실린다(실계정으로 잘못된 비밀번호를 넣어 확인).
 * <pre>
 * {"_SUBMIT_ERROR_":{"ERRMSG":"...","ERRTYPE":"LOGOUT","ERRCODE":"..."}}
 * </pre>
 * 인증되지 않은 요청은 ERRTYPE 이 LOGOUT 으로 온다. 서버가 미인증 상태를 로그아웃으로 취급하기 때문이다.
 */
public record SjptSubmitError(String errorType, String message) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ERROR_FIELD = "_SUBMIT_ERROR_";
    private static final String NOT_AUTHENTICATED_TYPE = "LOGOUT";

    public static Optional<SjptSubmitError> find(String responseBody) {
        try {
            JsonNode error = OBJECT_MAPPER.readTree(responseBody).path(ERROR_FIELD);
            if (error.isMissingNode() || !error.isObject()) {
                return Optional.empty();
            }
            return Optional.of(new SjptSubmitError(
                error.path("ERRTYPE").asText(""),
                error.path("ERRMSG").asText("")
            ));
        } catch (Exception exception) {
            // JSON 이 아닌 응답에는 이 봉투가 실리지 않는다. 다른 단계에서 형식을 검사한다.
            return Optional.empty();
        }
    }

    public boolean isNotAuthenticated() {
        return NOT_AUTHENTICATED_TYPE.equals(errorType);
    }
}
