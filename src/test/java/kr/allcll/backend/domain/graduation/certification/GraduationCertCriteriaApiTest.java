package kr.allcll.backend.domain.graduation.certification;

import static kr.allcll.backend.domain.user.AuthApi.LOGIN_SESSION;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GraduationCertificationApi.class)
class GraduationCertCriteriaApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GraduationCertCriteriaService graduationCertCriteriaService;

    @Test
    @DisplayName("졸업인증제도 기준 데이터를 조회한다.")
    void getGraduationCertCriteria() throws Exception {
        // given
        Long userId = 1L;

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(LOGIN_SESSION, userId);

        GraduationCertCriteriaResponse mockResponse = GraduationCertCriteriaResponse.of(
            null, null, null, null, null
        );

        when(graduationCertCriteriaService.getGraduationCertCriteria(userId)).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/graduation/certifications/criteria")
                .session(session))
            .andExpect(status().isOk());

        then(graduationCertCriteriaService).should().getGraduationCertCriteria(userId);
    }
}
