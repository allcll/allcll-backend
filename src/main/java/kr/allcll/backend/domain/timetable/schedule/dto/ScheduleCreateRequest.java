package kr.allcll.backend.domain.timetable.schedule.dto;

import java.util.List;
import kr.allcll.backend.domain.timetable.schedule.ScheduleType;
import org.antlr.v4.runtime.misc.NotNull;

public record ScheduleCreateRequest(
    String scheduleType,
    Long subjectId,
    String subjectName,
    String professorName,
    String location,
    List<TimeSlotDto> timeSlots
) {

}
