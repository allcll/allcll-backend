package kr.allcll.backend.domain.timetable.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.domain.timetable.TimeTable;
import kr.allcll.backend.domain.timetable.TimeTableRepository;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleCreateRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleResponse;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleUpdateRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.TimeTableDetailResponse;
import kr.allcll.backend.fixture.SubjectFixture;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.semester.Semester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ScheduleServiceTest {

    private static final String VALID_TOKEN = "adminToken";
    private static final String INVALID_TOKEN = "invalidToken";
    private static final Long NOT_FOUND_ID = 999L;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private OfficialScheduleRepository officialScheduleRepository;

    @Autowired
    private CustomScheduleRepository customScheduleRepository;

    @Autowired
    private TimeTableRepository timeTableRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    private TimeTable timeTable;
    private Subject subject;

    @BeforeEach
    void setUp() {
        officialScheduleRepository.deleteAll();
        customScheduleRepository.deleteAll();
        timeTableRepository.deleteAll();
        subjectRepository.deleteAll();

        timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        subject = subjectRepository.save(
            SubjectFixture.createSubject(
                "데이터베이스",
                "003278",
                "001",
                "변재욱")
        );
    }

    @Test
    @DisplayName("커스텀 스케줄을 정상 추가한다.")
    void addCustomSchedule() {
        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.CUSTOM,
            null,
            "커스텀 과목",
            "커스텀 교수",
            "커스텀 강의실 위치",
            "커스텀 요일",
            "09:00",
            "10:30"
        );

        ScheduleResponse response = scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN);

        assertThat(response.scheduleType()).isEqualTo("custom");
        assertThat(response.subjectId()).isNull();
        assertThat(response.subjectName()).isEqualTo("커스텀 과목");
        assertThat(response.professorName()).isEqualTo("커스텀 교수");
        assertThat(response.location()).isEqualTo("커스텀 강의실 위치");
        assertThat(response.dayOfWeeks()).isEqualTo("커스텀 요일");
        assertThat(response.startTime()).isEqualTo("09:00");
        assertThat(response.endTime()).isEqualTo("10:30");

        CustomSchedule saved = customScheduleRepository.findByIdAndTimeTableId(response.scheduleId(), timeTable.getId())
            .orElseThrow();
        assertThat(saved.getTimeTable().getId()).isEqualTo(timeTable.getId());
    }

    @Test
    @DisplayName("공식 스케줄을 정상 추가한다.")
    void addOfficialSchedule() {
        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.OFFICIAL,
            subject.getId(),
            null, null, null, null, null, null
        );

        ScheduleResponse response = scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN);

        assertThat(response.scheduleType()).isEqualTo("official");
        assertThat(response.subjectId()).isEqualTo(subject.getId());

        OfficialSchedule saved = officialScheduleRepository.findAllByTimeTableId(timeTable.getId()).get(0);
        assertThat(saved.getTimeTable().getId()).isEqualTo(timeTable.getId());
        assertThat(saved.getSubject().getId()).isEqualTo(subject.getId());
    }

    @Test
    @DisplayName("동일한 공식 스케줄을 중복 등록하면 예외가 발생한다.")
    void addDuplicateOfficialScheduleThrowsException() {
        // given
        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.OFFICIAL,
            subject.getId(),
            null, null, null, null, null, null
        );

        scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN);

        // when, then
        assertThatThrownBy(() ->
            scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN)
        )
            .isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.DUPLICATE_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 시간표에 추가 시 예외가 발생한다.")
    void addScheduleIfTimeTableNotFoundThrowsException() {
        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.CUSTOM,
            null,
            "커스텀 과목",
            "커스텀 교수님 성함",
            "커스텀 강의실 위치",
            "커스텀 요일",
            "09:00",
            "10:30"
        );
        assertThatThrownBy(() ->
            scheduleService.addSchedule(NOT_FOUND_ID, request, VALID_TOKEN)
        ).isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.TIMETABLE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("토큰 불일치 시 예외가 발생한다.")
    void addScheduleThrowsUnauthorizedAccess() {
        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.CUSTOM,
            null,
            "커스텀 과목",
            "커스텀 교수님 성함",
            "커스텀 강의실 위치",
            "커스텀 요일",
            "09:00",
            "10:30"
        );
        assertThatThrownBy(() ->
            scheduleService.addSchedule(timeTable.getId(), request, INVALID_TOKEN)
        ).isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("공식 일정 추가 시 해당 과목이 존재하지 않을 경우 예외가 발생한다.")
    void addOfficialScheduleThrowsSubjectNotFound() {
        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.OFFICIAL,
            NOT_FOUND_ID,
            null, null, null, null, null, null
        );
        assertThatThrownBy(() ->
            scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN)
        ).isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.SUBJECT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("특정 시간표의 일정 목록을 정상 조회한다.")
    void getTimeTableWithSchedule() {
        OfficialSchedule officialSchedule = officialScheduleRepository.save(new OfficialSchedule(timeTable, subject));
        CustomSchedule customSchedule = customScheduleRepository.save(
            new CustomSchedule(
                timeTable,
                "커스텀 과목",
                "커스텀 교수님 성함",
                "커스텀 강의실 위치",
                "커스텀 요일",
                LocalTime.of(9, 0),
                LocalTime.of(10, 30)
            ));

        TimeTableDetailResponse detailResponse = scheduleService.getTimeTableWithSchedules(
            timeTable.getId(), VALID_TOKEN
        );
        assertThat(detailResponse.timetableId()).isEqualTo(timeTable.getId());
        assertThat(detailResponse.schedules()).extracting("scheduleId")
            .containsExactly(officialSchedule.getId(), customSchedule.getId());
    }

    @Test
    @DisplayName("커스텀 일정의 모든 정보가 정상적으로 수정되는지 확인한다.")
    void updateEntireCustomSchedule() {
        CustomSchedule customSchedule = customScheduleRepository.save(
            new CustomSchedule(
                timeTable,
                "커스텀 과목",
                "커스텀 교수님 성함",
                "커스텀 강의실 위치",
                "커스텀 요일",
                LocalTime.of(9, 0),
                LocalTime.of(10, 30)
            ));
        ScheduleUpdateRequest request = new ScheduleUpdateRequest(
            "수정된 커스텀 과목",
            "수정된 교수님 성함",
            "수정된 강의실 위치",
            "수정된 요일",
            "10:30",
            "12:00"
        );

        ScheduleResponse response = scheduleService.updateSchedule(
            timeTable.getId(),
            customSchedule.getId(),
            request,
            VALID_TOKEN
        );
        assertThat(response.subjectName()).isEqualTo("수정된 커스텀 과목");
        assertThat(response.professorName()).isEqualTo("수정된 교수님 성함");
        assertThat(response.location()).isEqualTo("수정된 강의실 위치");
        assertThat(response.dayOfWeeks()).isEqualTo("수정된 요일");
        assertThat(response.startTime()).isEqualTo("10:30");
        assertThat(response.endTime()).isEqualTo("12:00");

        CustomSchedule updated = customScheduleRepository.findById(customSchedule.getId()).orElseThrow();
        assertThat(updated.getSubjectName()).isEqualTo("수정된 커스텀 과목");
    }

    @Test
    @DisplayName("커스텀 일정의 일부 정보가 정상적으로 수정되는지 확인한다.")
    void updatePartialCustomSchedule() {
        CustomSchedule customSchedule = customScheduleRepository.save(
            new CustomSchedule(
                timeTable,
                "커스텀 과목",
                "커스텀 교수님 성함",
                "커스텀 강의실 위치",
                "커스텀 요일",
                LocalTime.of(9, 0),
                LocalTime.of(10, 30)
            ));
        ScheduleUpdateRequest request = new ScheduleUpdateRequest(
            "수정된 커스텀 과목",
            "수정된 교수님 성함",
            null, null, null, null
        );

        ScheduleResponse response = scheduleService.updateSchedule(
            timeTable.getId(),
            customSchedule.getId(),
            request,
            VALID_TOKEN
        );
        assertThat(response.subjectName()).isEqualTo("수정된 커스텀 과목");
        assertThat(response.professorName()).isEqualTo("수정된 교수님 성함");
        assertThat(response.location()).isEqualTo("커스텀 강의실 위치");
        assertThat(response.dayOfWeeks()).isEqualTo("커스텀 요일");
        assertThat(response.startTime()).isEqualTo("09:00");
        assertThat(response.endTime()).isEqualTo("10:30");

        CustomSchedule updated = customScheduleRepository.findById(customSchedule.getId()).orElseThrow();
        assertThat(updated.getSubjectName()).isEqualTo("수정된 커스텀 과목");
    }

    @Test
    @DisplayName("커스텀 일정이 정상적으로 삭제되는지 확인한다.")
    void deleteSchedule() {
        CustomSchedule customSchedule = customScheduleRepository.save(
            new CustomSchedule(
                timeTable,
                "커스텀 과목",
                "커스텀 교수님 성함",
                "커스텀 강의실 위치",
                "커스텀 요일",
                LocalTime.of(9, 0),
                LocalTime.of(10, 30)
            ));
        scheduleService.deleteSchedule(timeTable.getId(), customSchedule.getId(), VALID_TOKEN);
        assertThat(customScheduleRepository.existsById(customSchedule.getId())).isFalse();
    }

    @Test
    @DisplayName("삭제할 일정이 존재하지 않을 경우 예외가 발생한다.")
    void deleteScheduleThrowsNotFound() {
        assertThatThrownBy(() ->
            scheduleService.deleteSchedule(timeTable.getId(), NOT_FOUND_ID, VALID_TOKEN)
        ).isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.SCHEDULE_NOT_FOUND.getMessage());
    }
}
