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
import kr.allcll.backend.support.semester.Semester;
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
                "semester": "FALL_25"
            }
            """;

        mockMvc.perform(post("/api/timetables")
                .cookie(TOKEN_COOKIE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("시간표 생성 시 존재하지 않는 학기 코드를 입력할 경우 400 반환을 검증한다.")
    void createTimeTable_invalidSemester_returns400() throws Exception {
        String json = """
        {
          "timeTableName": "시간표1",
          "semesterCode": "FALL_99"
        }
        """;

        mockMvc.perform(post("/api/timetables")
                .cookie(TOKEN_COOKIE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("시간표를 수정할 때 요청과 응답을 확인한다.")
    void updateTimeTable() throws Exception {
        Long timetableId = 1L;
        String json = """
            {
                "timeTableName": "새로운 시간표 제목"
            }
            """;

        String expected = """
        {
          "timeTableId": 1,
          "timeTableName": "새로운 시간표 제목",
          "semesterCode": "FALL_25",
          "semesterValue": "2025-2"
        }
        """;

        TimeTableResponse mockResponse = new TimeTableResponse(
            timetableId,
            "새로운 시간표 제목",
            "FALL_25",
            "2025-2"
        );
        when(timeTableService.updateTimeTable(timetableId, "새로운 시간표 제목", TOKEN))
            .thenReturn(mockResponse);

        MvcResult result = mockMvc.perform(patch("/api/timetables/{timetableId}", timetableId)
                .cookie(TOKEN_COOKIE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andReturn();

        System.out.println("응답 내용: " + result.getResponse().getContentAsString());

        assertThat(result.getResponse().getContentAsString())
            .isEqualToIgnoringWhitespace(expected);
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
                      "semesterCode": "FALL_25",
                      "semesterValue": "2025-2"
                    },
                    {
                      "timeTableId": 2,
                      "timeTableName": "시간표2",
                      "semesterCode": "FALL_25",
                      "semesterValue": "2025-2"
                    }
                ]
            }
            """;

        List<TimeTableResponse> list = List.of(
            new TimeTableResponse(1L, "시간표1", "FALL_25", "2025-2"),
            new TimeTableResponse(2L, "시간표2", "FALL_25", "2025-2")
        );
        when(timeTableService.getTimetables(TOKEN, Semester.FALL_25)).thenReturn(new TimeTablesResponse(list));

        MvcResult result = mockMvc.perform(get("/api/timetables")
                .cookie(TOKEN_COOKIE)
                .param("semesterCode", "FALL_25")
            )
            .andExpect(status().isOk())
            .andReturn();

        System.out.println("응답 내용: " + result.getResponse().getContentAsString());

        assertThat(result.getResponse().getContentAsString())
            .isEqualToIgnoringWhitespace(expected);
    }

    @Test
    @DisplayName("시간표 조회 시 학기 파라미터를 누락할 경우 404 반환을 검증한다.")
    void getTimetables_missingSemester_returns404() throws Exception {
        mockMvc.perform(get("/api/timetables")
                .cookie(TOKEN_COOKIE))
            .andExpect(status().isNotFound());
    }
}
