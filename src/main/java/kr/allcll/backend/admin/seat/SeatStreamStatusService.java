package kr.allcll.backend.admin.seat;

import java.util.Objects;
import kr.allcll.backend.admin.seat.dto.SeatStreamStatusResponse;
import kr.allcll.backend.support.sse.SseEventBuilderFactory;
import kr.allcll.backend.support.sse.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatStreamStatusService {

    private final SseService sseService;
    public final SeatStreamStatusStore seatStreamStatusStore;

    public void updateStatus(SeatStreamStatus newStatus) {
        SeatStreamStatus currentStatus = seatStreamStatusStore.getCurrentStatus();
        if (Objects.equals(currentStatus, newStatus)) {
            return;
        }
        seatStreamStatusStore.updateCurrentStatus(newStatus);

        SeatStreamStatusResponse sseStatusResponse = getSeatStreamStatusResponse(newStatus);
        sseService.propagate(SseEventBuilderFactory.EVENT_SEAT_STREAM_STATUS, sseStatusResponse);
    }

    private SeatStreamStatusResponse getSeatStreamStatusResponse(SeatStreamStatus newStatus) {
        return SeatStreamStatusResponse.of(
            newStatus.name().toLowerCase(),
            newStatus.getMessage()
        );
    }
}
