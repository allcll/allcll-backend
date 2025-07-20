package kr.allcll.backend.domain.timetable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import kr.allcll.backend.domain.timetable.dto.TimeTableResponse;
import kr.allcll.backend.domain.timetable.dto.TimeTablesResponse;
import kr.allcll.backend.support.exception.GlobalExceptionHandler;
import kr.allcll.backend.support.web.ThreadLocalHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(TimeTableApi.class)
@Import(GlobalExceptionHandler.class)
class TimeTableApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeTableService timeTableService;

    private static final String TOKEN = "mock-token";
    private static final Cookie TOKEN_COOKIE = new Cookie("token", TOKEN);

    @Test
    @DisplayName("시간표를 생성할 때 요청과 응답을 확인한다.")
    void createTimeTable() throws Exception {
        String json = """
            {
                "timetableName": "시간표 1",
                "semester": "2025-2"
            }
            """;

        mockMvc.perform(post("/api/timetables")
                .cookie(TOKEN_COOKIE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("시간표를 수정할 때 요청과 응답을 확인한다.")
    void updateTimeTable() throws Exception {
        Long timetableId = 1L;
        String json = """
            {
                "title": "새로운 시간표 제목"
            }
            """;

        TimeTableResponse mockResponse = new TimeTableResponse(timetableId, "새로운 시간표 제목", "2025-2");
        when(timeTableService.updateTimeTable(timetableId, "새로운 시간표 제목", TOKEN))
            .thenReturn(mockResponse);

        mockMvc.perform(patch("/api/timetables/{timetableId}", timetableId)
                .cookie(TOKEN_COOKIE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("시간표를 삭제할 때 요청과 응답을 확인한다.")
    void deleteTimeTable() throws Exception {
        Long timetableId = 1L;
        doNothing().when(timeTableService).deleteTimeTable(timetableId, TOKEN);

        mockMvc.perform(delete("/api/timetables/{timetableId}", timetableId)
                .cookie(TOKEN_COOKIE))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("시간표를 조회할 때 요청과 응답을 확인한다.")
    void getTimeTables() throws Exception {
        ThreadLocalHolder.SHARED_TOKEN.set(TOKEN);

        String expected = """
            {
                "timeTables": [
                    {
                      "timeTableId": 1,
                      "timeTableName": "시간표1",
                      "semester": "2025-2"
                    },
                    {
                      "timeTableId": 2,
                      "timeTableName": "시간표2",
                      "semester": "2025-2"
                    }
                ]
            }
            """;

        List<TimeTableResponse> list = List.of(
            new TimeTableResponse(1L, "시간표1", "2025-2"),
            new TimeTableResponse(2L, "시간표2", "2025-2")
        );
        when(timeTableService.getTimetables(TOKEN)).thenReturn(new TimeTablesResponse(list));

        MvcResult result = mockMvc.perform(get("/api/timetables")
                .cookie(TOKEN_COOKIE))
            .andExpect(status().isOk())
            .andReturn();

        System.out.println("응답 내용: " + result.getResponse().getContentAsString());

        assertThat(result.getResponse().getContentAsString())
            .isEqualToIgnoringWhitespace(expected);
    }
}
