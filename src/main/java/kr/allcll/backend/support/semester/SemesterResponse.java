package kr.allcll.backend.support.semester;

import java.time.LocalDate;

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
