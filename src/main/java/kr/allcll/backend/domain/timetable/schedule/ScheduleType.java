package kr.allcll.backend.domain.timetable.schedule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ScheduleType {
    OFFICIAL,
    CUSTOM;

    @JsonCreator
    public static ScheduleType from(String value) {
        return ScheduleType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
