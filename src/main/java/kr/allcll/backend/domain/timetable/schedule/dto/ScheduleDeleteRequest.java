package kr.allcll.backend.domain.timetable.schedule.dto;

import kr.allcll.backend.domain.timetable.schedule.ScheduleType;

public record ScheduleDeleteRequest(
    ScheduleType scheduleType
) {

}
