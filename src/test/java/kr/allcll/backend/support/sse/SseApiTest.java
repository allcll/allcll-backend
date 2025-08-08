package kr.allcll.backend.support.sse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.AdminSseApi;
import kr.allcll.backend.domain.seat.PinSeatSender;
import kr.allcll.backend.support.scheduler.SchedulerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminSseApi.class)
class SseApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private SchedulerService schedulerService;

    @MockitoBean
    private PinSeatSender pinSeatSender;

    @MockitoBean
    private AdminRequestValidator validator;

    @DisplayName("Server Sent Event를 연결한다.")
    @Test
    void getServerSentEventConnection() throws Exception {
        // when, then
        mockMvc.perform(get("/api/admin/sse-connect")
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
            .andExpect(status().isOk());
    }
}
