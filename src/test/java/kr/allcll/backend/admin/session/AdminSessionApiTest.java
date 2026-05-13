package kr.allcll.backend.admin.session;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.session.dto.SetCredentialRequest;
import kr.allcll.crawler.credential.Credentials;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminSessionApi.class)
class AdminSessionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private AdminRequestValidator validator;

    @MockitoBean
    private Credentials credentials;

    @Test
    @DisplayName("정상 요청은 자격 증명을 저장하고 200을 반환한다.")
    void setCredential() throws Exception {
        SetCredentialRequest request = new SetCredentialRequest("J", "U", "R", "L");
        when(validator.isRateLimited(any(HttpServletRequest.class))).thenReturn(false);
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);

        mockMvc.perform(post("/api/admin/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(sessionService).setCredential(request);
    }

    @Test
    @DisplayName("빈 JSON 요청은 400을 반환하고 서비스가 호출되지 않는다.")
    void setCredential_emptyJson() throws Exception {
        when(validator.isRateLimited(any(HttpServletRequest.class))).thenReturn(false);
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);

        mockMvc.perform(post("/api/admin/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());

        verify(sessionService, never()).setCredential(any());
    }

    @Test
    @DisplayName("토큰 일부가 누락된 요청은 400을 반환한다.")
    void setCredential_missingToken() throws Exception {
        when(validator.isRateLimited(any(HttpServletRequest.class))).thenReturn(false);
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        SetCredentialRequest request = new SetCredentialRequest("J", "U", "R", null);

        mockMvc.perform(post("/api/admin/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(sessionService, never()).setCredential(any());
    }

    @Test
    @DisplayName("토큰이 빈 문자열인 요청은 400을 반환한다.")
    void setCredential_blankToken() throws Exception {
        when(validator.isRateLimited(any(HttpServletRequest.class))).thenReturn(false);
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        SetCredentialRequest request = new SetCredentialRequest("J", "U", "R", "");

        mockMvc.perform(post("/api/admin/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(sessionService, never()).setCredential(any());
    }

    @Test
    @DisplayName("인증되지 않은 요청은 401을 반환한다.")
    void setCredential_unauthorized() throws Exception {
        SetCredentialRequest request = new SetCredentialRequest("J", "U", "R", "L");
        when(validator.isRateLimited(any(HttpServletRequest.class))).thenReturn(false);
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/admin/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증되지 않은 요청은 빈 body여도 401을 반환하고 서비스가 호출되지 않는다.")
    void setCredential_unauthorized_emptyBody() throws Exception {
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/admin/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());

        verify(sessionService, never()).setCredential(any());
    }
}
