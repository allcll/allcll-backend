package kr.allcll.backend.support.schedule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import org.springframework.stereotype.Component;

@Component
public class ScheduleStorage {

    private final Map<String, ScheduledFuture<?>> scheduledPinTasks = new ConcurrentHashMap<>();

    public boolean isAlreadyScheduled(String token) {
        return scheduledPinTasks.containsKey(token);
    }

    public void deleteSchedule(String token) {
        scheduledPinTasks.remove(token);
    }

    public void addSchedule(String token, ScheduledFuture<?> scheduledFuture) {
        scheduledPinTasks.put(token, scheduledFuture);
    }
}
