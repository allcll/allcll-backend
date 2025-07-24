package kr.allcll.backend.domain.timetable.schedule.dto;

public record TimeSlotDto(
    String dayOfWeeks,
    String startTime,
    String endTime
) {

}
