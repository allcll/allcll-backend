package kr.allcll.backend.domain.timetable;

import java.util.List;
import kr.allcll.backend.domain.timetable.dto.TimeTableCreateRequest;
import kr.allcll.backend.domain.timetable.dto.TimeTableResponse;
import kr.allcll.backend.domain.timetable.dto.TimeTablesResponse;
import kr.allcll.backend.domain.timetable.schedule.CustomScheduleRepository;
import kr.allcll.backend.domain.timetable.schedule.OfficialScheduleRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimeTableService {

    private final TimeTableRepository timeTableRepository;
    private final CustomScheduleRepository customScheduleRepository;
    private final OfficialScheduleRepository officialScheduleRepository;

    @Transactional
    public TimeTableResponse createTimeTable(String token, TimeTableCreateRequest request) {
        TimeTable timeTable = new TimeTable(token, request.timeTableName(), request.toSemester());
        timeTableRepository.save(timeTable);
        return TimeTableResponse.from(timeTable);
    }

    @Transactional
    public TimeTableResponse updateTimeTable(Long timetableId, String updatedTitle, String token) {
        TimeTable timeTable = timeTableRepository.findById(timetableId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.TIMETABLE_NOT_FOUND));
        validateTimeTableAccess(timeTable, token);
        timeTable.updateTimeTable(updatedTitle);
        return TimeTableResponse.from(timeTable);
    }

    @Transactional
    public void deleteTimeTable(Long timetableId, String token) {
        TimeTable timeTable = timeTableRepository.findById(timetableId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.TIMETABLE_NOT_FOUND));
        validateTimeTableAccess(timeTable, token);
        customScheduleRepository.deleteAllByTimeTableId(timeTable.getId());
        officialScheduleRepository.deleteAllByTimeTableId(timeTable.getId());
        timeTableRepository.delete(timeTable);
    }

    public TimeTablesResponse getTimetables(String token, String semesterValue) {
        Semester semester = Semester.fromValue(semesterValue);
        List<TimeTable> timeTables = timeTableRepository.findAllByTokenAndSemester(token, semester);

        return new TimeTablesResponse(
            timeTables.stream()
                .map(TimeTableResponse::from)
                .toList()
        );
    }

    private void validateTimeTableAccess(TimeTable timeTable, String token) {
        if (!token.equals(timeTable.getToken())) {
            throw new AllcllException(AllcllErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}
