package kr.allcll.backend.domain.department;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import kr.allcll.backend.domain.department.DepartmentApi;
import kr.allcll.backend.domain.department.DepartmentService;
import kr.allcll.backend.domain.department.dto.DepartmentResponse;
import kr.allcll.backend.domain.department.dto.DepartmentsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(DepartmentApi.class)
class DepartmentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    @DisplayName("과목 코드 전체 조회의 요청과 응답을 확인한다.")
    void retrieveAllDepartment() throws Exception {
        // given
        String expected = """
            {
            	"departments": [
            		{
            			"departmentName":"컴퓨터공학과",
            			"departmentCode":"3210"
            		},
            		{
            			"departmentName":"영어영문학전공",
            			"departmentCode":"2132"
            		}
            	]
            }
            """;

        // when
        when(departmentService.retrieveAllDepartment()).thenReturn(
            new DepartmentsResponse(
                List.of(
                    new DepartmentResponse("컴퓨터공학과", "3210"),
                    new DepartmentResponse("영어영문학전공", "2132")
                )
            )
        );
        MvcResult result = mockMvc.perform(get("/api/departments")).andExpect(status().isOk()).andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }
}
