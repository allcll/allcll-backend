package kr.allcll.backend.admin.preseat;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import kr.allcll.backend.admin.preseat.dto.PreSeatResponse;
import org.springframework.stereotype.Component;

@Component
public class AllPreSeatBuffer {

    private final Queue<PreSeatResponse> preSeatQueue;

    public AllPreSeatBuffer() {
        this.preSeatQueue = new ConcurrentLinkedQueue<>();
    }

    public void add(PreSeatResponse preSeat) {
        preSeatQueue.add(preSeat);
    }

    public void addAll(List<PreSeatResponse> preSeats) {
        preSeatQueue.addAll(preSeats);
    }

    public List<PreSeatResponse> getAllAndFlush() {
        List<PreSeatResponse> response = new ArrayList<>();
        PreSeatResponse item;
        while ((item = preSeatQueue.poll()) != null) {
            response.add(item);
        }
        return response;
    }
}

