package kr.allcll.backend.domain.timetable.dto;

public record TimeTableResponse (
        Long timetableId,
        String timetableName,
        String semester
){
}
