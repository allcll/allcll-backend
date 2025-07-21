package kr.allcll.backend.domain.timetable.schedule.dto;

public record ScheduleUpdateRequest(
    String subjectName,
    String professorName,
    String location,
    String dayOfWeeks,
    String startTime,
    String endTime
) {

}
