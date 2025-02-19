package kr.allcll.seatfinder.pin;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import kr.allcll.seatfinder.pin.dto.SubjectIdResponse;
import kr.allcll.seatfinder.pin.dto.SubjectIdsResponse;
import kr.allcll.seatfinder.sse.SseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(PinApi.class)
class PinApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PinService pinService;

    @MockitoBean
    private SseService sseService;

    private static final String BASE_URL = "/api/pin";

    @Test
    @DisplayName("핀 등록을 할 때에 요청과 응답을 확인한다.")
    void addPinOnSubjectWhenPinNotExist() throws Exception {
        // when, then
        mockMvc.perform(post(BASE_URL)
                .param("subjectId", "1"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("핀을 삭제할 때 요청과 응답을 확인한다.")
    void deletePinOnSubject() throws Exception {
        // when, then
        mockMvc.perform(delete(BASE_URL + "/{subjectId}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("핀을 조회할 때 요청과 응답을 확인한다.")
    void retrievePins() throws Exception {
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

        List<SubjectIdResponse> subjects = List.of(new SubjectIdResponse(1L), new SubjectIdResponse(2L));
        when(pinService.retrievePins("tokenValue"))
            .thenReturn(new SubjectIdsResponse(subjects));

        // when
        MvcResult result = mockMvc.perform(get("/api/pins")
                .cookie(new Cookie("token", "tokenValue")))
            .andExpect(status().isOk())
            .andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }
}
