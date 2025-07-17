package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.timetable.TimeTable;
import kr.allcll.backend.support.semester.Semester;

public class TimeTableFixture {

    public static TimeTable createTimeTable(String token, String timetableName, Semester semester) {
        return new TimeTable(token, timetableName, semester);
    }
}
