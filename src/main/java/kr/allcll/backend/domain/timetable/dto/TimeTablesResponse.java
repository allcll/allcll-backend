package kr.allcll.backend.domain.timetable.dto;

import java.util.List;

public record TimeTablesResponse (
        List<TimeTableResponse> timeTables
){
}
