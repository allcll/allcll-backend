package kr.allcll.backend.domain.timetable.dto;

import kr.allcll.backend.domain.timetable.TimeTable;
import kr.allcll.backend.support.semester.Semester;

public record TimeTableResponse(
    Long timeTableId,
    String timeTableName,
    Semester semester
) {

    public static TimeTableResponse from(TimeTable timeTable) {
        return new TimeTableResponse(
            timeTable.getId(),
            timeTable.getTimeTableName(),
            timeTable.getSemester()
        );
    }
}
