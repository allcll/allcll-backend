package kr.allcll.backend.domain.timetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kr.allcll.backend.domain.timetable.dto.TimeTableCreateRequest;
import kr.allcll.backend.domain.timetable.dto.TimeTableResponse;
import kr.allcll.backend.domain.timetable.dto.TimeTablesResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.semester.Semester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TimeTableServiceTest {

    private static final String TOKEN1 = "token1";
    private static final String TOKEN2 = "token2";

    @Autowired
    private TimeTableService timeTableService;
    @Autowired
    private TimeTableRepository timeTableRepository;

    @BeforeEach
    void setUp() {
        timeTableRepository.deleteAll();
    }

    @Test
    @DisplayName("시간표 정상 생성을 검증한다.")
    void createTimeTable() {
        // given
        TimeTableCreateRequest request1 = new TimeTableCreateRequest("새 시간표1", "FALL_25");
        TimeTableCreateRequest request2 = new TimeTableCreateRequest("새 시간표2", "FALL_25");

        // when
        TimeTableResponse timeTableResponse1 = timeTableService.createTimeTable(TOKEN1, request1);
        TimeTableResponse timeTableResponse2 = timeTableService.createTimeTable(TOKEN2, request2);

        // then
        TimeTable saved1 = timeTableRepository.findById(timeTableResponse1.timeTableId()).orElseThrow();
        TimeTable saved2 = timeTableRepository.findById(timeTableResponse2.timeTableId()).orElseThrow();

        assertThat(saved1.getToken()).isEqualTo(TOKEN1);
        assertThat(saved1.getTimeTableName()).isEqualTo("새 시간표1");
        assertThat(saved1.getSemester()).isEqualTo(Semester.FALL_25);

        assertThat(saved2.getToken()).isEqualTo(TOKEN2);
        assertThat(saved2.getTimeTableName()).isEqualTo("새 시간표2");
        assertThat(saved2.getSemester()).isEqualTo(Semester.FALL_25);
    }

    @Test
    @DisplayName("존재하지 않는 학기 코드가 들어오면 AllcllException이 발생한다")
    void createTimeTable_invalidSemester_throwsException() {
        // given
        String invalidSemester = "FALL_99";
        TimeTableCreateRequest request = new TimeTableCreateRequest("시간표1", invalidSemester);
        String token = "token";

        // when & then
        assertThatThrownBy(() -> timeTableService.createTimeTable(token, request))
            .isInstanceOf(AllcllException.class)
            .hasMessage("유효하지 않은 학기입니다.");
    }

    @Test
    @DisplayName("시간표 이름 정상 수정을 검증한다.")
    void updateTimeTable() {
        // given
        TimeTable timeTable = new TimeTable(TOKEN1, "기존 이름", Semester.FALL_25);
        timeTableRepository.saveAndFlush(timeTable);

        // when
        timeTableService.updateTimeTable(timeTable.getId(), "새로운 이름", TOKEN1);

        // then
        TimeTable updated = timeTableRepository.findByIdAndToken(timeTable.getId(), TOKEN1)
            .orElseThrow();
        assertThat(updated.getTimeTableName()).isEqualTo("새로운 이름");
    }

    @Test
    @DisplayName("시간표 이름 수정 시 토큰이 일치하지 않는 경우에 대한 예외를 검증한다.")
    void cannotUpdateTimeTable() {
        // given
        TimeTable timeTable = new TimeTable(TOKEN1, "기존 이름", Semester.FALL_25);
        timeTableRepository.saveAndFlush(timeTable);

        // when & then
        assertThatThrownBy(() -> timeTableService.updateTimeTable(timeTable.getId(), "새로운 이름", TOKEN2))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.UNAUTHORIZED_ACCESS.getMessage());

        TimeTable updated = timeTableRepository.findByIdAndToken(timeTable.getId(), TOKEN1)
            .orElseThrow();
        assertThat(updated.getTimeTableName()).isEqualTo("기존 이름");
    }

    @Test
    @DisplayName("시간표 정상 삭제를 검증한다")
    void deleteTimeTable() {
        // given
        TimeTable timeTable = new TimeTable(TOKEN1, "내 시간표", Semester.FALL_25);
        timeTableRepository.saveAndFlush(timeTable);

        // when
        timeTableService.deleteTimeTable(timeTable.getId(), TOKEN1);

        // then
        boolean exists = timeTableRepository.findById(timeTable.getId()).isPresent();
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("토큰이 일치하지 않는 경우 삭제에 대한 예외를 검증한다.")
    void deleteTimeTableDifferentToken() {
        // given
        TimeTable timeTable = new TimeTable(TOKEN1, "내 시간표", Semester.FALL_25);
        timeTableRepository.saveAndFlush(timeTable);

        // when & then
        assertThatThrownBy(() -> timeTableService.deleteTimeTable(timeTable.getId(), TOKEN2))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("시간표 조회 시 토큰과 학기 값이 모두 일치하는 시간표가 반환되는 것을 검증한다.")
    void getTimetables() {
        // given
        Semester semester = Semester.FALL_25;
        Semester otherSemester = Semester.SPRING_26;

        TimeTable timeTable1 = new TimeTable(TOKEN1, "내 시간표1", semester);
        TimeTable timeTable2 = new TimeTable(TOKEN1, "내 시간표2", semester);
        TimeTable otherSemesterTimeTable = new TimeTable(TOKEN1, "다른 학기 시간표", otherSemester);
        TimeTable otherTokenTimeTable = new TimeTable(TOKEN2, "다른 사람 시간표", semester);

        timeTableRepository.saveAll(List.of(timeTable1, timeTable2, otherSemesterTimeTable, otherTokenTimeTable));

        // when
        TimeTablesResponse timeTablesResponse = timeTableService.getTimetables(TOKEN1, semester.name());

        // then
        assertThat(timeTablesResponse.timeTables()).hasSize(2);
        assertThat(timeTablesResponse.timeTables())
            .extracting("timeTableName")
            .containsExactlyInAnyOrder("내 시간표1", "내 시간표2");
    }

    @Test
    @DisplayName("시간표 조회 시 학기 값이 없으면 예외가 발생한다.")
    void getTimetables_whenSemesterIsNull_throwsException() {
        // given
        Semester semester = Semester.FALL_25;
        Semester otherSemester = Semester.SPRING_26;

        TimeTable timeTable1 = new TimeTable(TOKEN1, "내 시간표1", semester);
        TimeTable timeTable2 = new TimeTable(TOKEN1, "내 시간표2", semester);
        TimeTable otherSemesterTimeTable = new TimeTable(TOKEN1, "다른 학기 시간표", otherSemester);
        TimeTable otherTokenTimeTable = new TimeTable(TOKEN2, "다른 사람 시간표", semester);

        timeTableRepository.saveAll(List.of(timeTable1, timeTable2, otherSemesterTimeTable, otherTokenTimeTable));

        // when & then
        assertThatThrownBy(() -> timeTableService.getTimetables(TOKEN1, null))
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.INVALID_SEMESTER.getMessage());
    }
}
