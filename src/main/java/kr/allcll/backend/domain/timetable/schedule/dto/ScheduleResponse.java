package kr.allcll.backend.domain.timetable.schedule.dto;

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
    String dayOfWeeks,
    String startTime,
    String endTime
) {

    public static ScheduleResponse fromOfficial(OfficialSchedule schedule, Subject subject) {
        return new ScheduleResponse(
            schedule.getId(),
            ScheduleType.OFFICIAL.toValue(),
            subject.getId(),
            null,
            null,
            null,
            null,
            null,
            null
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
            schedule.getDayOfWeeks(),
            schedule.getStartTime().toString(),
            schedule.getEndTime().toString()
        );
    }
}
