package kr.allcll.backend.domain.util;

import java.time.LocalDate;
import kr.allcll.backend.domain.util.dto.SemesterResponse;
import kr.allcll.backend.domain.util.dto.SemesterResponse.Period;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UtilApi.class)
class UtilApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UtilService utilService;

    @Test
    @DisplayName("학기 정보를 조회한다.")
    void getSemesterTest() throws Exception {
        // given
        String expected = """
            {
                "semester": "2025-1",
                "period": {
                    "startDate": "2025-02-01",
                    "endDate": "2025-03-31"
                }
            }
            """;

        when(utilService.getSemester())
            .thenReturn(new SemesterResponse(
                "2025-1",
                new Period(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 3, 31)))
            );

        // when, then
        mockMvc.perform(get("/api/service/semester"))
            .andExpect(status().isOk())
            .andExpect(content().json(expected));
    }
}
