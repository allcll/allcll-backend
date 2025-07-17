package kr.allcll.backend.domain.timetable.dto;

public record TimeTableCreateRequest(
        String timeTableName,
        String semester
) {
}
