package kr.allcll.backend.domain.timetable.schedule.dto;

import java.util.List;
import kr.allcll.backend.domain.timetable.TimeTable;

public record TimeTableDetailResponse(
    Long timetableId,
    String timetableName,
    String semester,
    List<ScheduleResponse> schedules
) {

    public static TimeTableDetailResponse from(TimeTable timeTable, List<ScheduleResponse> schedules) {
        return new TimeTableDetailResponse(
            timeTable.getId(),
            timeTable.getTimeTableName(),
            timeTable.getSemester().getValue(),
            schedules
        );
    }
}
