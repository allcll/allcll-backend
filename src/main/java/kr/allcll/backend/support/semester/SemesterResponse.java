package kr.allcll.backend.support.semester;

import java.time.LocalDate;

public record SemesterResponse(
    String semesterCode,
    String semesterValue,
    Period period
) {

    public static SemesterResponse from(Semester semester) {
        return new SemesterResponse(
            semester.name(),
            semester.getKoreanName(),
            new Period(semester.getStartDate(), semester.getEndDate())
        );
    }

    public record Period(
        LocalDate startDate,
        LocalDate endDate
    ) {

    }
}
