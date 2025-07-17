package kr.allcll.backend.domain.timetable;

import kr.allcll.backend.domain.timetable.dto.TimeTableCreateRequest;
import kr.allcll.backend.domain.timetable.dto.TimeTablesResponse;
import kr.allcll.backend.fixture.TimeTableFixture;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TimeTableServiceTest {

    private static final String TOKEN1 = "token1";
    private static final String TOKEN2 = "token2";
    private static final String EMPTY_TOKEN = " ";

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
        TimeTableCreateRequest request1 = new TimeTableCreateRequest("새 시간표1", "2025-1");
        TimeTableCreateRequest request2 = new TimeTableCreateRequest("새 시간표2", "2025-2");

        // when
        timeTableService.createTimeTable(TOKEN1, request1);
        timeTableService.createTimeTable(TOKEN2, request2);

        // then
        List<TimeTable> results = timeTableRepository.findAllByToken(TOKEN1);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTimeTableName()).isEqualTo("새 시간표1");
        assertThat(results.get(0).getSemester()).isEqualTo("2025-1");
    }

    @Test
    @DisplayName("시간표 생성 시 토큰이 비어있는 경우에 대한 예외를 검증한다.")
    void cannotCreateTimeTableByEmptyToken() {
        // given
        TimeTableCreateRequest request = new TimeTableCreateRequest("시간표", "2025-2");

        // when & then
        assertThatThrownBy(() -> timeTableService.createTimeTable(EMPTY_TOKEN, request))
                .isInstanceOf(AllcllException.class)
                .hasMessageContaining(AllcllErrorCode.TOKEN_INVALID.getMessage());
    }

    @Test
    @DisplayName("시간표 이름 정상 수정을 검증한다.")
    void updateTimeTable() {
        // given
        TimeTable timeTable = TimeTableFixture.createTimeTable(TOKEN1, "기존 이름", "2025-2");
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
        TimeTable timeTable = TimeTableFixture.createTimeTable(TOKEN1, "기존 이름", "2025-2");
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
        TimeTable timeTable = TimeTableFixture.createTimeTable(TOKEN1, "내 시간표", "2025-2");
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
        TimeTable timeTable = TimeTableFixture.createTimeTable(TOKEN1, "내 시간표", "2025-2");
        timeTableRepository.saveAndFlush(timeTable);

        // when & then
        assertThatThrownBy(() -> timeTableService.deleteTimeTable(timeTable.getId(), TOKEN2))
                .isInstanceOf(AllcllException.class)
                .hasMessageContaining(AllcllErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("토큰이 일치하는 시간표들을 검색하는 기능을 테스트한다.")
    void getTimetables() {
        // given
        TimeTable timeTable1 = TimeTableFixture.createTimeTable(TOKEN1, "내 시간표1", "2025-2");
        TimeTable timeTable2 = TimeTableFixture.createTimeTable(TOKEN1, "내 시간표2", "2025-2");
        TimeTable timeTable3 = TimeTableFixture.createTimeTable(TOKEN2, "다른 사람 시간표", "2025-2");
        timeTableRepository.saveAll(List.of(timeTable1, timeTable2, timeTable3));

        // when
        TimeTablesResponse timeTablesResponse = timeTableService.getTimetables(TOKEN1);

        // then
        assertThat(timeTablesResponse.timeTables().size()).isEqualTo(2);
    }

}