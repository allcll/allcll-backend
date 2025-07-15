package kr.allcll.backend.domain.timetable.dto;

public record TimeTableRequest (
        String timetableName,
        String semester
) {
}
