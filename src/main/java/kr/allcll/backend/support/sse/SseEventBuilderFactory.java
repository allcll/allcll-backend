package kr.allcll.backend.support.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.allcll.backend.admin.seat.SeatStreamStatus;
import kr.allcll.backend.admin.seat.dto.SeatStreamStatusResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

public class SseEventBuilderFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String EVENT_CONNECTION = "connection";
    public static final String EVENT_SEAT_STREAM_STATUS = "status";

    private static final long RECONNECT_TIME_MILLIS = 1000L;

    static {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static SseEventBuilder create(String eventName, Object value) {
        try {
            String sseEvent = objectMapper.writeValueAsString(value);
            return baseEvent()
                .name(eventName)
                .data(sseEvent, MediaType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static SseEventBuilder createInitialEvent() {
        return baseEvent()
            .name(EVENT_CONNECTION)
            .data("success", MediaType.TEXT_PLAIN);
    }

    public static SseEventBuilder createInitialSeatStreamStatusEvent(SeatStreamStatus seatStreamStatus) {
        SeatStreamStatusResponse seatStreamStatusResponse = SeatStreamStatusResponse.of(
            seatStreamStatus.name().toLowerCase(),
            seatStreamStatus.getMessage()
        );

        try {
            String sseEvent = objectMapper.writeValueAsString(seatStreamStatusResponse);
            return baseEvent()
                .name(EVENT_SEAT_STREAM_STATUS)
                .data(sseEvent, MediaType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static SseEventBuilder baseEvent() {
        return SseEmitter.event()
            .reconnectTime(RECONNECT_TIME_MILLIS);
    }
}
