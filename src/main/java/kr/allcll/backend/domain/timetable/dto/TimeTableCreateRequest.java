package kr.allcll.backend.domain.timetable.dto;

import kr.allcll.backend.support.semester.Semester;

public record TimeTableCreateRequest(
    String timeTableName,
    String semester
) {

    public Semester toSemester() {
        return Semester.fromCode(semester);
    }
}
