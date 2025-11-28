package kr.allcll.backend.admin.seat;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SeatSchedulerTracker {

    private final List<String> runningUserIds = new ArrayList<>();

    public synchronized void addUserId(String userId) {
        if (userId != null && !runningUserIds.contains(userId)) {
            runningUserIds.add(userId);
        }
    }

    public synchronized List<String> getUserIds() {
        return new ArrayList<>(runningUserIds);
    }

    public synchronized void clearAll() {
        runningUserIds.clear();
    }

    public synchronized void clear(String userId) {
        runningUserIds.remove(userId);
    }
}
