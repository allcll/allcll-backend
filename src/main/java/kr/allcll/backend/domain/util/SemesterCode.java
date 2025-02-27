package kr.allcll.backend.domain.util;

import java.time.LocalDate;
import java.util.Arrays;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.Getter;

@Getter
public enum SemesterCode {
    SPRING_25("2025-1", LocalDate.of(2025, 2, 1), LocalDate.of(2025, 3, 31)),
    SUMMER_25("2025-여름", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30)),
    FALL_25("2025-2", LocalDate.of(2025, 8, 1), LocalDate.of(2025, 9, 30)),
    WINTER_25("2025-겨울", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 31)),
    ;

    private final String value;
    private final LocalDate startDate;
    private final LocalDate endDate;

    SemesterCode(String value, LocalDate startDate, LocalDate endDate) {
        this.value = value;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static SemesterCode getCode(LocalDate date) {
        return Arrays.stream(values())
            .filter(semesterCode -> !semesterCode.startDate.isAfter(date))
            .filter(semesterCode -> !semesterCode.endDate.isBefore(date))
            .findFirst()
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SEMESTER_NOT_FOUND));
    }
}
