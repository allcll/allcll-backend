package kr.allcll.backend.admin.seat;

import java.util.ArrayList;
import java.util.List;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.springframework.stereotype.Component;

@Component
public class SeatSchedulerTracker {

    private final List<String> runningUserIds = new ArrayList<>();

    public synchronized void addUserId(String userId) {
        if (userId == null) {
            return;
        }
        if (!runningUserIds.isEmpty()) {
            throw new AllcllException(AllcllErrorCode.SEAT_CRAWLING_ALREADY_IN_PROGRESS);
        }
        runningUserIds.add(userId);
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
