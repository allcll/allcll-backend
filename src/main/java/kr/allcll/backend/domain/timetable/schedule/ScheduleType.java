package kr.allcll.backend.domain.timetable.schedule;

import java.util.Arrays;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.Getter;

@Getter
public enum ScheduleType {
    OFFICIAL("official"),
    CUSTOM("custom");

    private final String value;

    ScheduleType(String value) {
        this.value = value;
    }

    public static ScheduleType fromValue(String value) {
        return Arrays.stream(ScheduleType.values())
            .filter(s -> s.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.INVALID_SCHEDULE_TYPE));
    }
}
