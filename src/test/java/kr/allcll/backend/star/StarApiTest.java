package kr.allcll.backend.star;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import kr.allcll.backend.star.dto.StarredSubjectIdResponse;
import kr.allcll.backend.star.dto.StarredSubjectIdsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(StarApi.class)
class StarApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StarService starService;

    @Test
    @DisplayName("관담 기능의 즐겨찾기 등록을 할 때에 요청과 응답을 확인한다.")
    void addStarOnSubjectWhenStarNotExist() throws Exception {
        // when, then
        mockMvc.perform(post("/api/stars")
                .param("subjectId", "1"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관담 기능의 즐겨찾기를 삭제할 때 요청과 응답을 확인한다.")
    void deleteStarOnSubject() throws Exception {
        // when, then
        mockMvc.perform(delete("/api/stars/{subjectId}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관담기능을 조회할 때 요청과 응답을 확인한다.")
    void retrieveStars() throws Exception {
        // given
        String expected = """
                {
                    "subjects":[
                        {
                            "subjectId":1
                        },
                        {
                            "subjectId":2
                        }
                    ]
                }
            """;

        List<StarredSubjectIdResponse> subjects = List.of(
            new StarredSubjectIdResponse(1L),
            new StarredSubjectIdResponse(2L)
        );
        when(starService.retrieveStars("tokenValue"))
            .thenReturn(new StarredSubjectIdsResponse(subjects));

        // when
        MvcResult result = mockMvc.perform(get("/api/stars")
                .cookie(new Cookie("token", "tokenValue")))
            .andExpect(status().isOk())
            .andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }
}
