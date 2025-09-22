package kr.allcll.backend.admin.seat;

import org.springframework.stereotype.Component;

@Component
public class SeatStreamStatusStore {

    private volatile SeatStreamStatus currentStatus;

    public SeatStreamStatusStore() {
        this.currentStatus = SeatStreamStatus.IDLE;
    }

    public SeatStreamStatus getCurrentStatus() {
        return currentStatus;
    }

    public void updateCurrentStatus(SeatStreamStatus newStatus) {
        this.currentStatus = newStatus;
    }
}
