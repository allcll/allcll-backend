package kr.allcll.backend.domain.timetable.schedule;

import lombok.Getter;

@Getter
public enum ScheduleType {
    OFFICIAL("official"),
    CUSTOM("custom");

    private final String value;

    ScheduleType(String value) {
        this.value = value;
    }
}
