package kr.allcll.backend.domain.timetable.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleCreateRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleResponse;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleUpdateRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.TimeTableDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(ScheduleApi.class)
class ScheduleApiTest {

    private static final String BASE_URL = "/api/timetables";
    private static final String VALID_TOKEN = "adminToken";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleService scheduleService;

    @Test
    @DisplayName("시간표 일정에 커스텀 과목을 정상적으로 등록할 때 요청과 응답을 확인한다.")
    void addCustomSchedule() throws Exception {
        // given
        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.CUSTOM,
            null,
            "커스텀 과목",
            "커스텀 교수",
            "커스텀 강의실 위치",
            "커스텀 요일",
            "09:00",
            "10:30"
        );
        ScheduleResponse response = new ScheduleResponse(
            1L,
            ScheduleType.CUSTOM.toValue(),
            null,
            "커스텀 과목",
            "커스텀 교수",
            "커스텀 강의실 위치",
            "커스텀 요일",
            "09:00",
            "10:30"
        );
        when(scheduleService.addSchedule(eq(1L), any(ScheduleCreateRequest.class), eq(VALID_TOKEN)))
            .thenReturn(response);

        String expected = """
            {
                "scheduleId": 1,
                "scheduleType": "custom",
                "subjectId": null,
                "subjectName": "커스텀 과목",
                "professorName": "커스텀 교수",
                "location": "커스텀 강의실 위치",
                "dayOfWeeks": "커스텀 요일",
                "startTime": "09:00",
                "endTime": "10:30"
            }
            """;

        // when
        MvcResult result = mockMvc.perform(post(BASE_URL + "/{timeTableId}/schedules", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("token", VALID_TOKEN)))
            .andExpect(status().isCreated())
            .andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    @DisplayName("시간표 일정에 공식 과목을 정상적으로 등록할 때 요청과 응답을 확인한다.")
    void addOfficialSchedule() throws Exception {
        // given
        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.OFFICIAL,
            1L,
            "공식 과목",
            "공식 교수",
            null,
            null,
            null,
            null
        );
        ScheduleResponse response = new ScheduleResponse(
            1L,
            ScheduleType.OFFICIAL.toValue(),
            1L,
            null,
            null,
            null,
            null,
            null,
            null
        );
        when(scheduleService.addSchedule(eq(1L), any(ScheduleCreateRequest.class), eq(VALID_TOKEN)))
            .thenReturn(response);

        String expected = """
            {
                "scheduleId": 1,
                "scheduleType": "official",
                "subjectId": 1,
                "subjectName": null,
                "professorName": null,
                "location": null,
                "dayOfWeeks": null,
                "startTime": null,
                "endTime": null
            }
            """;

        // when
        MvcResult result = mockMvc.perform(post(BASE_URL + "/{timeTableId}/schedules", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("token", VALID_TOKEN)))
            .andExpect(status().isCreated())
            .andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    @DisplayName("특정 시간표의 등록된 일정 목록을 정상적으로 조회할 때 요청과 응답을 확인한다.")
    void getTimeTableWithSchedules() throws Exception {
        // given
        TimeTableDetailResponse response = new TimeTableDetailResponse(
            1L,
            "새 시간표",
            "2025-2",
            List.of(
                new ScheduleResponse(
                    1L,
                    ScheduleType.OFFICIAL.toValue(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                new ScheduleResponse(
                    2L,
                    ScheduleType.CUSTOM.toValue(),
                    null,
                    "커스텀 과목",
                    "커스텀 교수",
                    "커스텀 강의실 위치",
                    "커스텀 요일",
                    "09:00",
                    "10:30"
                )
            )
        );
        when(scheduleService.getTimeTableWithSchedules(eq(1L), eq(VALID_TOKEN)))
            .thenReturn(response);

        String expected = """
            {
                "timetableId": 1,
                "timetableName": "새 시간표",
                "semester": "2025-2",
                "schedules": [
                    {
                        "scheduleId": 1,
                        "scheduleType": "official",
                        "subjectId": 1,
                        "subjectName": null,
                        "professorName": null,
                        "location": null,
                        "dayOfWeeks": null,
                        "startTime": null,
                        "endTime": null
                    },
                    {
                        "scheduleId": 2,
                        "scheduleType": "custom",
                        "subjectId": null,
                        "subjectName": "커스텀 과목",
                        "professorName": "커스텀 교수",
                        "location": "커스텀 강의실 위치",
                        "dayOfWeeks": "커스텀 요일",
                        "startTime": "09:00",
                        "endTime": "10:30"
                    }
                ]
            }
            """;

        // when
        MvcResult result = mockMvc.perform(get(BASE_URL + "/{timeTableId}/schedules", 1L)
                .cookie(new Cookie("token", VALID_TOKEN)))
            .andExpect(status().isOk())
            .andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    @DisplayName("커스텀 스케줄의 전체 정보를 정상적으로 수정할 때 요청과 응답을 확인한다.")
    void updateEntireCustomSchedule() throws Exception {
        // given
        ScheduleUpdateRequest request = new ScheduleUpdateRequest(
            "수정된 커스텀 과목",
            "수정된 교수",
            "수정된 강의실 위치",
            "수정된 요일",
            "09:00",
            "10:30"
        );
        ScheduleResponse response = new ScheduleResponse(
            1L,
            ScheduleType.CUSTOM.toValue(),
            null,
            "수정된 커스텀 과목",
            "수정된 교수",
            "수정된 강의실 위치",
            "수정된 요일",
            "09:00",
            "10:30"
        );
        when(scheduleService.updateSchedule(eq(1L), eq(1L), any(ScheduleUpdateRequest.class), eq(VALID_TOKEN)))
            .thenReturn(response);

        String expected = """
            {
                "scheduleId": 1,
                "scheduleType": "custom",
                "subjectId": null,
                "subjectName": "수정된 커스텀 과목",
                "professorName": "수정된 교수",
                "location": "수정된 강의실 위치",
                "dayOfWeeks": "수정된 요일",
                "startTime": "09:00",
                "endTime": "10:30"
            }
            """;

        // when
        MvcResult result = mockMvc.perform(patch(BASE_URL + "/{timeTableId}/schedules/{scheduleId}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("token", VALID_TOKEN)))
            .andExpect(status().isOk())
            .andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    @DisplayName("커스텀 스케줄의 일부 정보를 정상적으로 수정할 때 요청과 응답을 확인한다.")
    void updatePartialCustomSchedule() throws Exception {
        // given
        ScheduleUpdateRequest request = new ScheduleUpdateRequest(
            "수정된 커스텀 과목",
            "수정된 교수님 성함",
            "수정된 강의실 위치",
            null,
            null,
            null
        );
        ScheduleResponse response = new ScheduleResponse(
            1L,
            ScheduleType.CUSTOM.toValue(),
            null,
            "수정된 커스텀 과목",
            "수정된 교수님 성함",
            "수정된 강의실 위치",
            "기존 요일",
            "09:00",
            "10:30"
        );
        when(scheduleService.updateSchedule(eq(1L), eq(1L), any(ScheduleUpdateRequest.class), eq(VALID_TOKEN)))
            .thenReturn(response);

        String expected = """
            {
                "scheduleId": 1,
                "scheduleType": "custom",
                "subjectId": null,
                "subjectName": "수정된 커스텀 과목",
                "professorName": "수정된 교수님 성함",
                "location": "수정된 강의실 위치",
                "dayOfWeeks": "기존 요일",
                "startTime": "09:00",
                "endTime": "10:30"
            }
            """;

        // when
        MvcResult result = mockMvc.perform(patch(BASE_URL + "/{timeTableId}/schedules/{scheduleId}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("token", VALID_TOKEN)))
            .andExpect(status().isOk())
            .andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    @DisplayName("일정을 정상적으로 삭제할 때의 요청과 응답을 확인한다.")
    void deleteSchedule() throws Exception {
        // given
        doNothing().when(scheduleService).deleteSchedule(eq(1L), eq(1L), eq(VALID_TOKEN));

        // when, then
        mockMvc.perform(delete(BASE_URL + "/{timeTableId}/schedules/{scheduleId}", 1L, 1L)
                .cookie(new Cookie("token", VALID_TOKEN)))
            .andExpect(status().isNoContent());
    }
}
