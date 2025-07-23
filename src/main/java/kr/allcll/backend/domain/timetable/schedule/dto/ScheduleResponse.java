package kr.allcll.backend.domain.timetable.schedule.dto;

import java.util.Collections;
import java.util.List;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.timetable.schedule.CustomSchedule;
import kr.allcll.backend.domain.timetable.schedule.OfficialSchedule;
import kr.allcll.backend.domain.timetable.schedule.ScheduleType;

public record ScheduleResponse(
    Long scheduleId,
    String scheduleType,
    Long subjectId,
    String subjectName,
    String professorName,
    String location,
    List<TimeSlotDto> timeSlots
) {

    public static ScheduleResponse fromOfficial(OfficialSchedule schedule, Subject subject) {
        return new ScheduleResponse(
            schedule.getId(),
            ScheduleType.OFFICIAL.toValue(),
            subject.getId(),
            null,
            null,
            null,
            Collections.emptyList()
        );
    }

    public static ScheduleResponse fromCustom(CustomSchedule schedule) {
        return new ScheduleResponse(
            schedule.getId(),
            ScheduleType.CUSTOM.toValue(),
            null,
            schedule.getSubjectName(),
            schedule.getProfessorName(),
            schedule.getLocation(),
            schedule.getTimeSlots()
        );
    }
}
