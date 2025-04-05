package kr.allcll.backend.domain.util.dto;

import java.time.LocalDate;
import kr.allcll.backend.domain.util.Semester;

public record SemesterResponse(
    String code,
    String semester,
    Period period
) {

    public static SemesterResponse from(Semester semester) {
        return new SemesterResponse(
            semester.name(),
            semester.getValue(),
            new Period(semester.getStartDate(), semester.getEndDate())
        );
    }

    public record Period(
        LocalDate startDate,
        LocalDate endDate
    ) {

    }
}
