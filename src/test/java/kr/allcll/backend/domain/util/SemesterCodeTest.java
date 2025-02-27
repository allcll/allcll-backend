package kr.allcll.backend.domain.util;

import java.time.LocalDate;
import kr.allcll.backend.support.exception.AllcllException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SemesterCodeTest {

    @Test
    @DisplayName("학기 시작일에 해당하는 코드가 반환되어야 한다.")
    void shouldReturnCorrectSemesterCodeOnStartDate() {
        LocalDate springStart = LocalDate.of(2025, 2, 1);
        LocalDate summerStart = LocalDate.of(2025, 6, 1);
        LocalDate fallStart = LocalDate.of(2025, 8, 1);
        LocalDate winterStart = LocalDate.of(2025, 12, 1);

        assertAll(
            () -> assertThat(SemesterCode.getCode(springStart)).isEqualTo(SemesterCode.SPRING_25),
            () -> assertThat(SemesterCode.getCode(summerStart)).isEqualTo(SemesterCode.SUMMER_25),
            () -> assertThat(SemesterCode.getCode(fallStart)).isEqualTo(SemesterCode.FALL_25),
            () -> assertThat(SemesterCode.getCode(winterStart)).isEqualTo(SemesterCode.WINTER_25)
        );
    }

    @Test
    @DisplayName("학기 종료일에 해당하는 코드가 반환되어야 한다.")
    void shouldReturnCorrectSemesterCodeOnEndDate() {
        LocalDate springEnd = LocalDate.of(2025, 3, 31);
        LocalDate summerEnd = LocalDate.of(2025, 6, 30);
        LocalDate fallEnd = LocalDate.of(2025, 9, 30);
        LocalDate winterEnd = LocalDate.of(2025, 12, 31);

        assertAll(
            () -> assertThat(SemesterCode.getCode(springEnd)).isEqualTo(SemesterCode.SPRING_25),
            () -> assertThat(SemesterCode.getCode(summerEnd)).isEqualTo(SemesterCode.SUMMER_25),
            () -> assertThat(SemesterCode.getCode(fallEnd)).isEqualTo(SemesterCode.FALL_25),
            () -> assertThat(SemesterCode.getCode(winterEnd)).isEqualTo(SemesterCode.WINTER_25)
        );
    }

    @Test
    @DisplayName("학기 기간 내 날짜에 대해 올바른 학기 코드가 반환되어야 한다.")
    void shouldReturnCorrectSemesterCodeWithinPeriod() {
        LocalDate springMiddle = LocalDate.of(2025, 2, 15);
        LocalDate summerMiddle = LocalDate.of(2025, 6, 15);
        LocalDate fallMiddle = LocalDate.of(2025, 8, 15);
        LocalDate winterMiddle = LocalDate.of(2025, 12, 15);

        assertAll(
            () -> assertThat(SemesterCode.getCode(springMiddle)).isEqualTo(SemesterCode.SPRING_25),
            () -> assertThat(SemesterCode.getCode(summerMiddle)).isEqualTo(SemesterCode.SUMMER_25),
            () -> assertThat(SemesterCode.getCode(fallMiddle)).isEqualTo(SemesterCode.FALL_25),
            () -> assertThat(SemesterCode.getCode(winterMiddle)).isEqualTo(SemesterCode.WINTER_25)
        );
    }

    @Test
    @DisplayName("학기 시작일 하루 전 날짜는 예외를 발생시켜야 한다.")
    void shouldThrowExceptionWhenBeforeStartDate() {
        LocalDate beforeSpring = LocalDate.of(2025, 1, 31);
        LocalDate beforeSummer = LocalDate.of(2025, 5, 31);
        LocalDate beforeFall = LocalDate.of(2025, 7, 31);
        LocalDate beforeWinter = LocalDate.of(2025, 11, 30);

        assertAll(
            () -> assertThatThrownBy(() -> SemesterCode.getCode(beforeSpring))
                .isInstanceOf(AllcllException.class),
            () -> assertThatThrownBy(() -> SemesterCode.getCode(beforeSummer))
                .isInstanceOf(AllcllException.class),
            () -> assertThatThrownBy(() -> SemesterCode.getCode(beforeFall))
                .isInstanceOf(AllcllException.class),
            () -> assertThatThrownBy(() -> SemesterCode.getCode(beforeWinter))
                .isInstanceOf(AllcllException.class)
        );
    }

    @Test
    @DisplayName("학기 종료일 하루 후 날짜는 예외를 발생시켜야 한다.")
    void shouldThrowExceptionWhenAfterEndDate() {
        LocalDate afterSpring = LocalDate.of(2025, 4, 1);
        LocalDate afterSummer = LocalDate.of(2025, 7, 1);
        LocalDate afterFall = LocalDate.of(2025, 10, 1);
        LocalDate afterWinter = LocalDate.of(2026, 1, 1);

        assertAll(
            () -> assertThatThrownBy(() -> SemesterCode.getCode(afterSpring))
                .isInstanceOf(AllcllException.class),
            () -> assertThatThrownBy(() -> SemesterCode.getCode(afterSummer))
                .isInstanceOf(AllcllException.class),
            () -> assertThatThrownBy(() -> SemesterCode.getCode(afterFall))
                .isInstanceOf(AllcllException.class),
            () -> assertThatThrownBy(() -> SemesterCode.getCode(afterWinter))
                .isInstanceOf(AllcllException.class)
        );
    }

    @Test
    @DisplayName("학기와 학기 사이의 날짜는 예외를 발생시켜야 한다.")
    void shouldThrowExceptionWhenBetweenSemesters() {
        LocalDate betweenSpringAndSummer = LocalDate.of(2025, 4, 15);
        LocalDate betweenSummerAndFall = LocalDate.of(2025, 7, 15);
        LocalDate betweenFallAndWinter = LocalDate.of(2025, 10, 15);

        assertAll(
            () -> assertThatThrownBy(() -> SemesterCode.getCode(betweenSpringAndSummer))
                .isInstanceOf(AllcllException.class),
            () -> assertThatThrownBy(() -> SemesterCode.getCode(betweenSummerAndFall))
                .isInstanceOf(AllcllException.class),
            () -> assertThatThrownBy(() -> SemesterCode.getCode(betweenFallAndWinter))
                .isInstanceOf(AllcllException.class)
        );
    }
}
