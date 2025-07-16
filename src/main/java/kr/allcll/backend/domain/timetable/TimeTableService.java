package kr.allcll.backend.domain.timetable;

import kr.allcll.backend.domain.timetable.dto.TimeTableResponse;
import kr.allcll.backend.domain.timetable.dto.TimeTablesResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeTableService {

    private final TimeTableRepository timeTableRepository;

    @Transactional
    public void createTimeTable(String token, String timeTableName, String semester) {
        validateToken(token);

        TimeTable timeTable = new TimeTable(token, timeTableName, semester);
        timeTableRepository.save(timeTable);
    }

    @Transactional
    public TimeTableResponse updateTimeTable(Long timetableId, String newTitle, String token) {
        TimeTable timeTable = validateTimetable(timetableId, token);

        timeTable.updateTimeTable(newTitle);

        return TimeTableResponse.from(timeTable);
    }

    @Transactional
    public void deleteTimeTable(Long timetableId, String token) {
        TimeTable timeTable = validateTimetable(timetableId, token);

        timeTableRepository.delete(timeTable);
    }

    @Transactional(readOnly = true)
    public TimeTablesResponse getTimetables(String token) {
        List<TimeTable> timeTables = timeTableRepository.findByToken(token)
                .orElseThrow(() -> new AllcllException(AllcllErrorCode.TOKEN_INVALID));
        return new TimeTablesResponse(
                timeTables.stream()
                        .map(timeTable -> new TimeTableResponse(
                                timeTable.getId(),
                                timeTable.getTimeTableName(),
                                timeTable.getSemester()))
                        .toList()
        );
    }

    public void validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AllcllException(AllcllErrorCode.TOKEN_INVALID);
        }
    }

    public TimeTable validateTimetable(Long timetableId, String token) {
        return timeTableRepository.findByIdAndToken(timetableId, token)
                .orElseThrow(() -> new AllcllException(AllcllErrorCode.TIMETABLE_NOT_FOUND));
    }

}
