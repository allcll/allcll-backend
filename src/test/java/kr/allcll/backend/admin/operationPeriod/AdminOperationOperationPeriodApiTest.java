package kr.allcll.backend.admin.operationPeriod;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.operationPeriod.dto.OperationPeriodRequest;
import kr.allcll.backend.domain.operationPeriod.OperationType;
import kr.allcll.backend.support.semester.Semester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminOperationPeriodApi.class)
class AdminOperationOperationPeriodApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminOperationPeriodService operationPeriodService;

    @MockitoBean
    private AdminRequestValidator validator;

    @Test
    @DisplayName("운영 기간을 저장한다.")
    void saveOperationPeriod() throws Exception {
        // given
        OperationPeriodRequest request = new OperationPeriodRequest(
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 2, 1, 9, 0),
            LocalDateTime.of(2025, 2, 7, 18, 0),
            "2025-1학기 수강신청 기간"
        );

        when(validator.isRateLimited(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(false);
        when(validator.isUnauthorized(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(false);
        doNothing().when(operationPeriodService)
            .saveOperationPeriod(Semester.SPRING_25, request);

        // when, then
        mockMvc.perform(post("/api/admin/operation-period")
                .param("semester", "SPRING_25")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증되지 않은 요청은 401을 반환한다.")
    void saveOperationPeriod_unauthorized() throws Exception {
        // given
        OperationPeriodRequest request = new OperationPeriodRequest(
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 2, 1, 9, 0),
            LocalDateTime.of(2025, 2, 7, 18, 0),
            "2025-1학기 수강신청 기간"
        );

        when(validator.isRateLimited(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(false);
        when(validator.isUnauthorized(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(true);

        // when, then
        mockMvc.perform(post("/api/admin/operation-period")
                .param("semester", "SPRING_25")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("요청 제한에 걸린 경우 401을 반환한다.")
    void saveOperationPeriod_rateLimited() throws Exception {
        // given
        OperationPeriodRequest request = new OperationPeriodRequest(
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 2, 1, 9, 0),
            LocalDateTime.of(2025, 2, 7, 18, 0),
            "2025-1학기 수강신청 기간"
        );

        when(validator.isRateLimited(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(true);
        when(validator.isUnauthorized(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(false);

        // when, then
        mockMvc.perform(post("/api/admin/operation-period")
                .param("semester", "SPRING_25")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("운영 기간을 삭제한다.")
    void deleteOperationPeriod() throws Exception {
        // given
        when(validator.isRateLimited(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(false);
        when(validator.isUnauthorized(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(false);
        doNothing().when(operationPeriodService)
            .deleteOperationPeriod(Semester.SPRING_25, OperationType.TIMETABLE);

        // when, then
        mockMvc.perform(delete("/api/admin/operation-period")
                .param("semester", "SPRING_25")
                .param("operationType", "TIMETABLE"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("삭제 시 인증되지 않은 요청은 401을 반환한다.")
    void deleteOperationPeriod_unauthorized() throws Exception {
        // given
        when(validator.isRateLimited(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(false);
        when(validator.isUnauthorized(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(true);

        // when, then
        mockMvc.perform(delete("/api/admin/operation-period")
                .param("semester", "SPRING_25")
                .param("operationType", "TIMETABLE"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("삭제 시 요청 제한에 걸린 경우 401을 반환한다.")
    void deleteOperationPeriod_rateLimited() throws Exception {
        // given
        when(validator.isRateLimited(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(true);
        when(validator.isUnauthorized(org.mockito.ArgumentMatchers.any(HttpServletRequest.class)))
            .thenReturn(false);

        // when, then
        mockMvc.perform(delete("/api/admin/operation-period")
                .param("semester", "SPRING_25")
                .param("operationType", "TIMETABLE"))
            .andExpect(status().isUnauthorized());
    }
}
