package kr.allcll.backend.domain.timetable;

import java.util.List;
import kr.allcll.backend.domain.timetable.dto.TimeTableCreateRequest;
import kr.allcll.backend.domain.timetable.dto.TimeTableResponse;
import kr.allcll.backend.domain.timetable.dto.TimeTablesResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimeTableService {

    private final TimeTableRepository timeTableRepository;

    @Transactional
    public void createTimeTable(String token, TimeTableCreateRequest request) {
        TimeTable timeTable = new TimeTable(token, request.timeTableName(), request.toSemester());
        timeTableRepository.save(timeTable);
    }

    @Transactional
    public TimeTableResponse updateTimeTable(Long timetableId, String updatedTitle, String token) {
        TimeTable timeTable = getTimeTableById(timetableId);
        validateTimeTableAccess(timeTable, token);
        timeTable.updateTimeTable(updatedTitle);
        return TimeTableResponse.from(timeTable);
    }

    @Transactional
    public void deleteTimeTable(Long timetableId, String token) {
        TimeTable timeTable = getTimeTableById(timetableId);
        validateTimeTableAccess(timeTable, token);
        timeTableRepository.delete(timeTable);
    }

    public TimeTablesResponse getTimetables(String token) {
        List<TimeTable> timeTables = timeTableRepository.findAllByToken(token);
        return new TimeTablesResponse(
            timeTables.stream()
                .map(TimeTableResponse::from)
                .toList()
        );
    }

    private TimeTable getTimeTableById(Long timetableId) {
        return timeTableRepository.findById(timetableId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.TIMETABLE_NOT_FOUND));
    }

    private void validateTimeTableAccess(TimeTable timeTable, String token) {
        if (!token.equals(timeTable.getToken())) {
            throw new AllcllException(AllcllErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}
