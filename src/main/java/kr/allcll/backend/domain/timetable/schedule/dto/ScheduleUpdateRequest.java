package kr.allcll.backend.domain.timetable.schedule.dto;

import java.util.List;

public record ScheduleUpdateRequest(
    String subjectName,
    String professorName,
    String location,
    List<TimeSlotDto> timeSlots
) {

}
