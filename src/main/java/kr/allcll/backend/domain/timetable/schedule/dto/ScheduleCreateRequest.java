package kr.allcll.backend.domain.timetable.schedule.dto;

import kr.allcll.backend.domain.timetable.schedule.ScheduleType;
import org.antlr.v4.runtime.misc.NotNull;

public record ScheduleCreateRequest(
    @NotNull
    ScheduleType scheduleType,
    Long subjectId,
    String subjectName,
    String professorName,
    String location,
    String dayOfWeeks,
    String startTime,
    String endTime
) {

}
