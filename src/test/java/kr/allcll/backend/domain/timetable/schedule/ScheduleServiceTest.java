package kr.allcll.backend.domain.timetable.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;

import java.util.Collections;
import java.util.List;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.domain.timetable.TimeTable;
import kr.allcll.backend.domain.timetable.TimeTableRepository;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleCreateRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleDeleteRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleResponse;
import kr.allcll.backend.domain.timetable.schedule.dto.ScheduleUpdateRequest;
import kr.allcll.backend.domain.timetable.schedule.dto.TimeSlotDto;
import kr.allcll.backend.domain.timetable.schedule.dto.TimeTableDetailResponse;
import kr.allcll.backend.fixture.SubjectFixture;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.semester.Semester;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void clean() {
        officialScheduleRepository.deleteAllInBatch();
        customScheduleRepository.deleteAllInBatch();
        timeTableRepository.deleteAllInBatch();
        subjectRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("커스텀 일정 추가 시 올바른 응답을 반환한다.")
    void addCustomScheduleReturnsValidResponse() {
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        TimeSlotDto timeSlot = new TimeSlotDto(
            "월",
            "09:00",
            "10:30"
        );

        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.CUSTOM.getValue(),
            null,
            "커스텀 과목",
            "커스텀 교수",
            "커스텀 강의실 위치",
            List.of(timeSlot)
        );

        //when
        ScheduleResponse response = scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN);

        //then
        assertThat(response.scheduleType()).isEqualTo("custom");
        assertThat(response.subjectId()).isNull();
        assertThat(response.subjectName()).isEqualTo("커스텀 과목");
        assertThat(response.professorName()).isEqualTo("커스텀 교수");
        assertThat(response.location()).isEqualTo("커스텀 강의실 위치");
        assertThat(response.timeSlots()).hasSize(1)
            .extracting(
                TimeSlotDto::dayOfWeeks,
                TimeSlotDto::startTime,
                TimeSlotDto::endTime
            )
            .containsExactly(
                tuple("월", "09:00", "10:30"));
    }

    @Test
    @DisplayName("커스텀 일정 추가 후 실제 DB에 저장되는지 확인한다.")
    void addCustomSchedulePersistsToDatabase() {
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        TimeSlotDto timeSlot = new TimeSlotDto(
            "월",
            "09:00",
            "10:30"
        );

        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.CUSTOM.getValue(),
            null,
            "커스텀 과목",
            "커스텀 교수",
            "커스텀 강의실 위치",
            List.of(timeSlot)
        );

        ScheduleResponse response = scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN);

        //when
        CustomSchedule saved = customScheduleRepository
            .findByIdAndTimeTableId(response.scheduleId(), timeTable.getId())
            .orElseThrow();

        //then
        assertThat(saved.getSubjectName()).isEqualTo("커스텀 과목");
        assertThat(saved.getProfessorName()).isEqualTo("커스텀 교수");
        assertThat(saved.getLocation()).isEqualTo("커스텀 강의실 위치");
        assertThat(saved.getTimeTable().getId()).isEqualTo(timeTable.getId());
    }

    @Test
    @DisplayName("공식 스케줄 추가 시 올바른 응답을 반환한다.")
    void addOfficialScheduleReturnsValidResponse() {
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        Subject subject = subjectRepository.save(
            SubjectFixture.createSubject(
                "데이터베이스",
                "003278",
                "001",
                "변재욱")
        );

        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.OFFICIAL.getValue(),
            subject.getId(),
            null, null, null,
            Collections.emptyList()
        );

        //when
        ScheduleResponse response = scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN);

        //then
        assertThat(response.scheduleType()).isEqualTo("official");
        assertThat(response.subjectId()).isEqualTo(subject.getId());
    }

    @Test
    @DisplayName("공식 스케줄 추가 후 실제 DB에 저장되는지 확인한다.")
    void addOfficialSchedulePersistsToDatabase() {
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        Subject subject = subjectRepository.save(
            SubjectFixture.createSubject(
                "데이터베이스",
                "003278",
                "001",
                "변재욱")
        );

        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.OFFICIAL.getValue(),
            subject.getId(),
            null, null, null,
            Collections.emptyList()
        );

        scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN);

        //when
        OfficialSchedule saved = officialScheduleRepository.findAllByTimeTableId(timeTable.getId()).get(0);

        //then
        assertThat(saved.getTimeTable().getId()).isEqualTo(timeTable.getId());
        assertThat(saved.getSubject().getId()).isEqualTo(subject.getId());
    }

    @Test
    @DisplayName("동일한 공식 스케줄을 중복 등록하면 예외가 발생한다.")
    void addDuplicateOfficialScheduleThrowsException() {
        // given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        Subject subject = subjectRepository.save(
            SubjectFixture.createSubject(
                "데이터베이스",
                "003278",
                "001",
                "변재욱")
        );

        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.OFFICIAL.getValue(),
            subject.getId(),
            null, null, null,
            Collections.emptyList()
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
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        Subject subject = subjectRepository.save(
            SubjectFixture.createSubject(
                "데이터베이스",
                "003278",
                "001",
                "변재욱")
        );
        TimeSlotDto timeSlot = new TimeSlotDto(
            "월",
            "09:00",
            "10:30"
        );

        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.CUSTOM.getValue(),
            null,
            "커스텀 과목",
            "커스텀 교수님 성함",
            "커스텀 강의실 위치",
            List.of(timeSlot)
        );

        //when,then
        assertThatThrownBy(() ->
            scheduleService.addSchedule(NOT_FOUND_ID, request, VALID_TOKEN)
        ).isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.TIMETABLE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("토큰 불일치 시 예외가 발생한다.")
    void addScheduleThrowsUnauthorizedAccess() {
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        TimeSlotDto timeSlot = new TimeSlotDto(
            "월",
            "09:00",
            "10:30"
        );
        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.CUSTOM.getValue(),
            null,
            "커스텀 과목",
            "커스텀 교수님 성함",
            "커스텀 강의실 위치",
            List.of(timeSlot)
        );

        //when, then
        assertThatThrownBy(() ->
            scheduleService.addSchedule(timeTable.getId(), request, INVALID_TOKEN)
        ).isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("공식 일정 추가 시 해당 과목이 존재하지 않을 경우 예외가 발생한다.")
    void addOfficialScheduleThrowsSubjectNotFound() {
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        Subject subject = subjectRepository.save(
            SubjectFixture.createSubject(
                "데이터베이스",
                "003278",
                "001",
                "변재욱")
        );
        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.OFFICIAL.getValue(),
            NOT_FOUND_ID,
            null, null, null,
            Collections.emptyList()
        );

        //when, then
        assertThatThrownBy(() ->
            scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN)
        ).isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.SUBJECT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("커스텀 일정 추가 시 시작 시간이 종료 시간보다 늦은 경우 예외가 발생한다.")
    void addCustomScheduleIfStartTimeIsLaterThanEndTimeThrowsException() {
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        TimeSlotDto timeSlot = new TimeSlotDto(
            "월",
            "10:30",
            "09:00"
        );

        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.CUSTOM.getValue(),
            null,
            "커스텀 과목",
            "커스텀 교수",
            "커스텀 강의실 위치",
            List.of(timeSlot)
        );

        //when, then
        assertThatThrownBy(() ->
            scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN)
        ).isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.INVALID_TIME.getMessage());
    }

    @Test
    @DisplayName("커스텀 일정 추가 시 시작 시간과 종료 시간이 같을 경우 예외가 발생한다.")
    void addCustomScheduleIfStartTimeIsSameAsEndTimeThrowsException() {
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        TimeSlotDto timeSlot = new TimeSlotDto(
            "월",
            "10:30",
            "10:30"
        );

        ScheduleCreateRequest request = new ScheduleCreateRequest(
            ScheduleType.CUSTOM.getValue(),
            null,
            "커스텀 과목",
            "커스텀 교수",
            "커스텀 강의실 위치",
            List.of(timeSlot)
        );

        //when, then
        assertThatThrownBy(() ->
            scheduleService.addSchedule(timeTable.getId(), request, VALID_TOKEN)
        ).isInstanceOf(AllcllException.class)
            .hasMessageContaining(AllcllErrorCode.INVALID_TIME.getMessage());
    }

    @Test
    @DisplayName("특정 시간표의 일정 목록을 정상 조회한다.")
    void getTimeTableWithSchedule() {
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        Subject subject = subjectRepository.save(
            SubjectFixture.createSubject(
                "데이터베이스",
                "003278",
                "001",
                "변재욱")
        );
        TimeSlotDto timeSlot = new TimeSlotDto(
            "월",
            "09:00",
            "10:30"
        );

        OfficialSchedule officialSchedule = officialScheduleRepository.save(new OfficialSchedule(timeTable, subject));
        CustomSchedule customSchedule = customScheduleRepository.save(
            new CustomSchedule(
                timeTable,
                "커스텀 과목",
                "커스텀 교수님 성함",
                "커스텀 강의실 위치",
                List.of(timeSlot)
            ));

        //when
        TimeTableDetailResponse detailResponse = scheduleService.getTimeTableWithSchedules(
            timeTable.getId(), VALID_TOKEN
        );

        //then
        assertThat(detailResponse.timetableId()).isEqualTo(timeTable.getId());
        assertThat(detailResponse.schedules()).extracting("scheduleId")
            .containsExactly(officialSchedule.getId(), customSchedule.getId());
    }

    @Test
    @DisplayName("커스텀 일정의 모든 정보가 정상적으로 수정되는지 확인한다.")
    void updateEntireCustomSchedule() {
        //given
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        TimeSlotDto existingTimeSlot = new TimeSlotDto(
            "월",
            "09:00",
            "10:30"
        );
        TimeSlotDto updateTimeSlot = new TimeSlotDto(
            "화",
            "10:30",
            "12:00"
        );
        CustomSchedule customSchedule = customScheduleRepository.save(
            new CustomSchedule(
                timeTable,
                "커스텀 과목",
                "커스텀 교수님 성함",
                "커스텀 강의실 위치",
                List.of(existingTimeSlot)
            ));
        ScheduleUpdateRequest request = new ScheduleUpdateRequest(
            "수정된 커스텀 과목",
            "수정된 교수님 성함",
            "수정된 강의실 위치",
            List.of(updateTimeSlot)
        );

        //when
        ScheduleResponse response = scheduleService.updateSchedule(
            timeTable.getId(),
            customSchedule.getId(),
            request,
            VALID_TOKEN
        );

        //then
        assertThat(response.subjectName()).isEqualTo("수정된 커스텀 과목");
        assertThat(response.professorName()).isEqualTo("수정된 교수님 성함");
        assertThat(response.location()).isEqualTo("수정된 강의실 위치");
        assertThat(response.timeSlots()).hasSize(1)
            .extracting(
                TimeSlotDto::dayOfWeeks,
                TimeSlotDto::startTime,
                TimeSlotDto::endTime
            )
            .containsExactly(
                tuple("화", "10:30", "12:00"));
        CustomSchedule updated = customScheduleRepository.findById(customSchedule.getId()).orElseThrow();
        assertThat(updated.getSubjectName()).isEqualTo("수정된 커스텀 과목");
    }

    @Test
    @DisplayName("공식 일정이 정상적으로 삭제되는지 확인한다.")
    void deleteOfficialSchedule() {
        //given
        ScheduleDeleteRequest request = new ScheduleDeleteRequest(
            ScheduleType.OFFICIAL.getValue()
        );
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        Subject subject = subjectRepository.save(
            SubjectFixture.createSubject(
                "데이터베이스",
                "003278",
                "001",
                "변재욱")
        );
        OfficialSchedule officialSchedule = officialScheduleRepository.save(
            new OfficialSchedule(
                timeTable,
                subject
            )
        );

        //when
        scheduleService.deleteSchedule(timeTable.getId(), officialSchedule.getId(), request, VALID_TOKEN);

        //then
        assertThat(customScheduleRepository.existsById(officialSchedule.getId())).isFalse();
    }

    @Test
    @DisplayName("커스텀 일정이 정상적으로 삭제되는지 확인한다.")
    void deleteCustomSchedule() {
        //given
        ScheduleDeleteRequest request = new ScheduleDeleteRequest(
            ScheduleType.CUSTOM.getValue()
        );
        TimeTable timeTable = timeTableRepository.save(
            new TimeTable(
                VALID_TOKEN,
                "테스트 시간표",
                Semester.FALL_25
            )
        );
        TimeSlotDto timeSlot = new TimeSlotDto(
            "월",
            "09:00",
            "10:30"
        );
        CustomSchedule customSchedule = customScheduleRepository.save(
            new CustomSchedule(
                timeTable,
                "커스텀 과목",
                "커스텀 교수님 성함",
                "커스텀 강의실 위치",
                List.of(timeSlot)
            ));

        //when
        scheduleService.deleteSchedule(timeTable.getId(), customSchedule.getId(), request, VALID_TOKEN);

        //then
        assertThat(customScheduleRepository.existsById(customSchedule.getId())).isFalse();
    }
}
