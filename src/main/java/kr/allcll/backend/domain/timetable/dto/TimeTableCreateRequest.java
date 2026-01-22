package kr.allcll.backend.domain.timetable.dto;

import kr.allcll.backend.support.semester.Semester;

public record TimeTableCreateRequest(
    String timeTableName,
    Semester semester
) {

}
