package kr.allcll.backend.support.schedule;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future.State;
import java.util.concurrent.ScheduledFuture;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Setter
@Component
public class ScheduleStorage {

    private final Map<String, ScheduledFuture<?>> scheduledPinTasks = new ConcurrentHashMap<>();
    private Optional<ScheduledFuture<?>> nonMajorSchedule;

    public ScheduleStorage() {
        this.nonMajorSchedule = Optional.empty();
    }

    public void cancelNonMajorSchedule() {
        nonMajorSchedule.ifPresent(scheduledFuture -> scheduledFuture.cancel(true));
    }

    public boolean isNonMajorScheduleRunning() {
        return nonMajorSchedule
            .map(scheduledFuture -> scheduledFuture.state().equals(State.RUNNING))
            .orElse(false);
    }

    public boolean isNonMajorScheduleNotRunning() {
        return !isNonMajorScheduleRunning();
    }

    public boolean isAlreadyScheduledPin(String token) {
        return scheduledPinTasks.containsKey(token);
    }

    public void deletePinSchedule(String token) {
        scheduledPinTasks.remove(token);
    }

    public void addPinSchedule(String token, ScheduledFuture<?> scheduledFuture) {
        scheduledPinTasks.put(token, scheduledFuture);
    }
}
