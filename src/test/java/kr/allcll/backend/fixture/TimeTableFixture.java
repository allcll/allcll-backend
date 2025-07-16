package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.basket.Basket;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.timetable.TimeTable;

public class TimeTableFixture {

    public static TimeTable createTimeTable(String token, String timetableName, String semester) {
        return new TimeTable(token, timetableName, semester);
    }
}
