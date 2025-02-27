package kr.allcll.backend.domain.basket;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import kr.allcll.backend.domain.basket.BasketApi;
import kr.allcll.backend.domain.basket.BasketService;
import kr.allcll.backend.domain.basket.dto.BasketsEachSubject;
import kr.allcll.backend.domain.basket.dto.BasketsResponse;
import kr.allcll.backend.domain.basket.dto.EachDepartmentBasket;
import kr.allcll.backend.domain.basket.dto.SubjectBasketsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(BasketApi.class)
public class BasketApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BasketService basketService;

    @Test
    @DisplayName("관심과목 전체 조회의 요청과 응답을 확인한다.")
    void findAllBasket() throws Exception {
        // given
        String expected = """
            {
                "baskets": [
                    {
                        "subjectId": 1,
                        "subjectName": "컴퓨터구조",
                        "departmentName": "전자정보공학과",
                        "departmentCode": "3210",
                        "subjectCode": "004310",
                        "classCode": "001",
                        "professorName": "김보예",
                        "totalCount": 14
                    }
                ]
            }
            """;

        // when
        when(basketService.findBasketsByCondition(null, null, null)).thenReturn(
            new BasketsResponse(List.of(
                new BasketsEachSubject(1L, "컴퓨터구조",
                    "전자정보공학과", "3210",
                    "004310", "001", "김보예", 14)
            ))
        );
        MvcResult result = mockMvc.perform(get("/api/baskets")).andExpect(status().isOk()).andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    @DisplayName("특정 관심과목 조회의 요청과 응답을 확인한다.")
    void findBasket() throws Exception {
        // given
        String expected = """
            {
                "everytimeLectureId": 2864827,
                "eachDepartmentRegisters": [
                    {
                        "studentBelong": "본교생",
                        "registerDepartment": "컴퓨터공학과",
                        "eachCount": 10
                    }
                ]
            }
            """;

        // when
        when(basketService.getEachSubjectBaskets(1L)).thenReturn(
            new SubjectBasketsResponse(
                2864827L,
                List.of(
                    new EachDepartmentBasket("본교생", "컴퓨터공학과", 10)
                ))
        );
        MvcResult result = mockMvc.perform(get("/api/baskets/1")).andExpect(status().isOk()).andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    @DisplayName("관심과목으로 등록한 학생이 없으면, 관심과목 조회에 빈 응답을 확인한다.")
    void findBasketEmpty() throws Exception {
        // given
        String expected = """
            {
                "everytimeLectureId": 5412311,
                "eachDepartmentRegisters": []
            }
            """;

        // when
        when(basketService.getEachSubjectBaskets(1L)).thenReturn(
            new SubjectBasketsResponse(
                5412311L,
                List.of())
        );
        MvcResult result = mockMvc.perform(get("/api/baskets/1")).andExpect(status().isOk()).andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }
}
