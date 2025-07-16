package kr.allcll.backend.domain.timetable;

import kr.allcll.backend.domain.timetable.dto.TimeTableRequest;
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
    public void createTimeTable(String token, TimeTableRequest request) {
        validateToken(token);

        TimeTable timeTable = new TimeTable(token, request.timetableName(), request.semester());
        timeTableRepository.save(timeTable);
    }

    @Transactional
    public TimeTableResponse updateTimeTable(Long timetableId, String newTitle, String token) {
        validateToken(token);

        TimeTable timeTable = validateAndGetTimeTable(timetableId, token);
        timeTable.updateTimeTable(newTitle);

        return TimeTableResponse.from(timeTable);
    }

    @Transactional
    public void deleteTimeTable(Long timetableId, String token) {
        validateToken(token);

        TimeTable timeTable = validateAndGetTimeTable(timetableId, token);
        timeTableRepository.delete(timeTable);
    }

    @Transactional(readOnly = true)
    public TimeTablesResponse getTimetables(String token) {
        validateToken(token);

        List<TimeTable> timeTables = timeTableRepository.findAllByToken(token);
        if (timeTables.isEmpty()) {
            throw new AllcllException(AllcllErrorCode.TOKEN_INVALID);
        }

        return new TimeTablesResponse(
                timeTables.stream()
                        .map(TimeTableResponse::from)
                        .toList()
        );
    }

    private void validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AllcllException(AllcllErrorCode.TOKEN_INVALID);
        }
    }

    private TimeTable validateAndGetTimeTable(Long timetableId, String token) {
        TimeTable timeTable = timeTableRepository.findById(timetableId)
                .orElseThrow(() -> new AllcllException(AllcllErrorCode.TIMETABLE_NOT_FOUND));

        if (!timeTable.getToken().equals(token)) {
            throw new AllcllException(AllcllErrorCode.UNAUTHORIZED_ACCESS);
        }

        return timeTable;
    }

}
