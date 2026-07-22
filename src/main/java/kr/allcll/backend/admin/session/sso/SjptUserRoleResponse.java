package kr.allcll.backend.admin.session.sso;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;

/**
 * 프로그램 role 등록(initUserRole.do) 응답.
 * <p>
 * 이 시스템은 실패해도 HTTP 200 과 HTML 을 돌려주고, 성공 응답의 본문에도 "실패", "error" 같은 문자열이 들어 있다(실계정 캡처로 확인). 따라서 상태 코드나 본문 문자열 포함
 * 여부로는 성공을 판정할 수 없고, 응답이 요청한 프로그램 키를 그대로 돌려주는지로 판정한다.
 */
public record SjptUserRoleResponse(String programKey) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static SjptUserRoleResponse from(String responseBody) {
        try {
            JsonNode menuCode = OBJECT_MAPPER.readTree(responseBody).path("dm_UserRole").path("MENU_CD");
            if (!menuCode.isTextual() || menuCode.asText().isBlank()) {
                throw new AllcllException(AllcllErrorCode.SSO_PROGRAM_ROLE_REGISTER_FAIL);
            }
            return new SjptUserRoleResponse(menuCode.asText());
        } catch (JsonProcessingException exception) {
            throw new AllcllException(AllcllErrorCode.SSO_PROGRAM_ROLE_REGISTER_FAIL, exception);
        }
    }

    /**
     * 등록을 요청한 프로그램과 다른 프로그램이 돌아오면 권한이 붙지 않은 것으로 본다.
     */
    public void validateRegisteredFor(String requestedProgramKey) {
        if (!programKey.equals(requestedProgramKey)) {
            throw new AllcllException(AllcllErrorCode.SSO_PROGRAM_ROLE_REGISTER_FAIL);
        }
    }
}
