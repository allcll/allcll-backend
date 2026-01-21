package kr.allcll.backend.domain.timetable.dto;

import kr.allcll.backend.domain.timetable.TimeTable;

public record TimeTableResponse(
    Long timeTableId,
    String timeTableName,
    String semesterCode,
    String semesterValue
) {

    public static TimeTableResponse from(TimeTable timeTable) {
        return new TimeTableResponse(
            timeTable.getId(),
            timeTable.getTimeTableName(),
            timeTable.getSemester().name(),
            timeTable.getSemester().getValue()
        );
    }
}
