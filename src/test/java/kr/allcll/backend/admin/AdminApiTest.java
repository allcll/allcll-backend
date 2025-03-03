package kr.allcll.backend.admin;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.allcll.backend.admin.dto.SystemStatusResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(AdminApi.class)
class AdminApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @Test
    @DisplayName("시스템 설정 값 조회 api의 요청과 응답을 확인한다.")
    void getSystemStatus() throws Exception {
        // given
        String expected = """
                {
                    "isSseConnect": false,
                    "isNonMajorSending": false
                }
            """;

        // when
        when(adminService.getSystemStatus()).thenReturn(
            new SystemStatusResponse(false, false)
        );
        MvcResult result = mockMvc
            .perform(get("/admin/system-status"))
            .andExpect(status().isOk())
            .andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }
}
