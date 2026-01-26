package kr.allcll.backend.support.semester;

import java.time.LocalDate;
import java.util.Arrays;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.Getter;

@Getter
public enum Semester {
    /*
    2025년 1학기 코드 수정할 경우에 [참고](https://github.com/allcll/allcll-backend/pull/121#discussion_r2118843479)
     */
    SPRING_25("2025/1학기", LocalDate.of(2025, 2, 1), LocalDate.of(2025, 3, 31)),
    SUMMER_25("2025-여름", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30)),
    FALL_25("2025-2", LocalDate.of(2025, 8, 1), LocalDate.of(2025, 11, 30)),
    WINTER_25("2025-겨울", LocalDate.of(2025, 12, 1), LocalDate.of(2026, 1, 22)),
    SPRING_26("2026-1", LocalDate.of(2026, 1, 23), LocalDate.of(2026, 6, 30)),
    ;

    private final String koreanName;
    private final LocalDate startDate;
    private final LocalDate endDate;

    Semester(String koreanName, LocalDate startDate, LocalDate endDate) {
        this.koreanName = koreanName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // TODO: 분기 규칙 책임 과다 -> Service 코드 분리 필요
    public static Semester findByDate(LocalDate date) {
        return Arrays.stream(values())
            .filter(semester -> isDateInRange(semester, date))
            .findFirst()
            .orElseGet(() -> getNextSemester(date));
    }

    private static boolean isDateInRange(Semester semester, LocalDate date) {
        LocalDate startDate = semester.getStartDate();
        LocalDate endDate = semester.getEndDate();
        return !startDate.isAfter(date) && !endDate.isBefore(date);
    }

    private static Semester getNextSemester(LocalDate date) {
        return Arrays.stream(values())
            .filter(semester -> semester.getStartDate().isAfter(date))
            .findFirst()
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SEMESTER_NOT_FOUND));
    }

    public static String getCurrentSemester() {
        return findByDate(LocalDate.now()).getKoreanName();
    }
}
