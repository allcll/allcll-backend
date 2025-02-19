package kr.allcll.seatfinder.sse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.allcll.seatfinder.seat.SeatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SseApi.class)
class SseApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private SeatService seatService;

    @DisplayName("Server Sent Event를 연결한다.")
    @Test
    void getServerSentEventConnection() throws Exception {
        // when, then
        mockMvc.perform(get("/api/connect")
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
            .andExpect(status().isOk());
    }
}
