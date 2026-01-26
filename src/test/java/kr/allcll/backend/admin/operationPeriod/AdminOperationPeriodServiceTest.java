package kr.allcll.backend.admin.operationPeriod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.allcll.backend.admin.operationPeriod.dto.OperationPeriodRequest;
import kr.allcll.backend.domain.operationPeriod.OperationPeriod;
import kr.allcll.backend.domain.operationPeriod.OperationType;
import kr.allcll.backend.domain.operationPeriod.PeriodRepository;
import kr.allcll.backend.support.semester.Semester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class AdminOperationPeriodServiceTest {

    @Autowired
    private AdminOperationPeriodService adminOperationPeriodService;

    @Autowired
    private PeriodRepository periodRepository;

    @AfterEach
    void tearDown() {
        periodRepository.deleteAll();
    }

    @Test
    @DisplayName("새로운 운영 기간을 저장한다.")
    void saveOperationPeriod_new() {
        // given
        OperationPeriodRequest request = new OperationPeriodRequest(
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 2, 1, 9, 0),
            LocalDateTime.of(2025, 2, 7, 18, 0),
            "2025-1학기 수강신청 기간"
        );

        // when
        adminOperationPeriodService.saveOperationPeriod(Semester.SPRING_25, request);

        // then
        Optional<OperationPeriod> foundPeriod = periodRepository.findBySemesterAndOperationType(
            Semester.SPRING_25,
            OperationType.TIMETABLE
        );

        assertThat(foundPeriod).isPresent();
        assertThat(foundPeriod.get().getSemester()).isEqualTo(Semester.SPRING_25);
        assertThat(foundPeriod.get().getOperationType()).isEqualTo(OperationType.TIMETABLE);
        assertThat(foundPeriod.get().getStartDate()).isEqualTo(LocalDateTime.of(2025, 2, 1, 9, 0));
        assertThat(foundPeriod.get().getEndDate()).isEqualTo(LocalDateTime.of(2025, 2, 7, 18, 0));
        assertThat(foundPeriod.get().getMessage()).isEqualTo("2025-1학기 수강신청 기간");
    }

    @Test
    @DisplayName("이미 존재하는 운영 기간을 업데이트한다.")
    void saveOperationPeriod_update() {
        // given
        OperationPeriod existingPeriod = OperationPeriod.create(
            Semester.SPRING_25,
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 2, 1, 9, 0),
            LocalDateTime.of(2025, 2, 7, 18, 0),
            "기존 메시지"
        );
        periodRepository.save(existingPeriod);

        OperationPeriodRequest updateRequest = new OperationPeriodRequest(
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 2, 3, 10, 0),
            LocalDateTime.of(2025, 2, 10, 17, 0),
            "수정된 메시지"
        );

        // when
        adminOperationPeriodService.saveOperationPeriod(Semester.SPRING_25, updateRequest);

        // then
        Optional<OperationPeriod> foundPeriod = periodRepository.findBySemesterAndOperationType(
            Semester.SPRING_25,
            OperationType.TIMETABLE
        );

        assertThat(foundPeriod).isPresent();
        assertThat(foundPeriod.get().getId()).isEqualTo(existingPeriod.getId());
        assertThat(foundPeriod.get().getStartDate()).isEqualTo(LocalDateTime.of(2025, 2, 3, 10, 0));
        assertThat(foundPeriod.get().getEndDate()).isEqualTo(LocalDateTime.of(2025, 2, 10, 17, 0));
        assertThat(foundPeriod.get().getMessage()).isEqualTo("수정된 메시지");
    }

    @Test
    @DisplayName("다른 학기의 동일 운영 타입은 별도로 저장된다.")
    void saveOperationPeriod_differentSemester() {
        // given
        OperationPeriodRequest spring25Request = new OperationPeriodRequest(
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 2, 1, 9, 0),
            LocalDateTime.of(2025, 2, 7, 18, 0),
            "2025-1학기 수강신청"
        );

        OperationPeriodRequest summer25Request = new OperationPeriodRequest(
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 6, 1, 9, 0),
            LocalDateTime.of(2025, 6, 7, 18, 0),
            "2025-여름학기 수강신청"
        );

        // when
        adminOperationPeriodService.saveOperationPeriod(Semester.SPRING_25, spring25Request);
        adminOperationPeriodService.saveOperationPeriod(Semester.SUMMER_25, summer25Request);

        // then
        Optional<OperationPeriod> spring25Period = periodRepository.findBySemesterAndOperationType(
            Semester.SPRING_25,
            OperationType.TIMETABLE
        );
        Optional<OperationPeriod> summer25Period = periodRepository.findBySemesterAndOperationType(
            Semester.SUMMER_25,
            OperationType.TIMETABLE
        );

        assertThat(spring25Period).isPresent();
        assertThat(summer25Period).isPresent();
        assertThat(spring25Period.get().getId()).isNotEqualTo(summer25Period.get().getId());
    }

    @Test
    @DisplayName("운영 기간을 삭제한다.")
    void deleteOperationPeriod() {
        // given
        OperationPeriod period = OperationPeriod.create(
            Semester.SPRING_25,
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 2, 1, 9, 0),
            LocalDateTime.of(2025, 2, 7, 18, 0),
            "2025-1학기 수강신청 기간"
        );
        periodRepository.save(period);

        // when
        adminOperationPeriodService.deleteOperationPeriod(Semester.SPRING_25, OperationType.TIMETABLE);

        // then
        Optional<OperationPeriod> foundPeriod = periodRepository.findBySemesterAndOperationType(
            Semester.SPRING_25,
            OperationType.TIMETABLE
        );
        assertThat(foundPeriod).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 운영 기간을 삭제해도 예외가 발생하지 않는다.")
    void deleteOperationPeriod_notExist() {
        // when & then
        assertThatCode(
            () -> adminOperationPeriodService.deleteOperationPeriod(Semester.SPRING_25, OperationType.TIMETABLE)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("특정 학기와 운영 타입의 기간만 삭제하고 다른 기간은 유지한다.")
    void deleteOperationPeriod_onlyTarget() {
        // given
        OperationPeriod targetPeriod = OperationPeriod.create(
            Semester.SPRING_25,
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 2, 1, 9, 0),
            LocalDateTime.of(2025, 2, 7, 18, 0),
            "수강신청 기간"
        );

        OperationPeriod otherTypePeriod = OperationPeriod.create(
            Semester.SPRING_25,
            OperationType.BASKETS,
            LocalDateTime.of(2025, 2, 8, 9, 0),
            LocalDateTime.of(2025, 2, 14, 18, 0),
            "수강변경 기간"
        );

        OperationPeriod otherSemesterPeriod = OperationPeriod.create(
            Semester.SUMMER_25,
            OperationType.TIMETABLE,
            LocalDateTime.of(2025, 6, 1, 9, 0),
            LocalDateTime.of(2025, 6, 7, 18, 0),
            "여름학기 수강신청"
        );

        periodRepository.save(targetPeriod);
        periodRepository.save(otherTypePeriod);
        periodRepository.save(otherSemesterPeriod);

        // when
        adminOperationPeriodService.deleteOperationPeriod(Semester.SPRING_25, OperationType.TIMETABLE);

        // then
        assertThat(periodRepository.findBySemesterAndOperationType(
            Semester.SPRING_25,
            OperationType.TIMETABLE
        )).isEmpty();

        assertThat(periodRepository.findBySemesterAndOperationType(
            Semester.SPRING_25,
            OperationType.BASKETS
        )).isPresent();

        assertThat(periodRepository.findBySemesterAndOperationType(
            Semester.SUMMER_25,
            OperationType.TIMETABLE
        )).isPresent();
    }
}
