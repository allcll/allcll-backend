package kr.allcll.backend.domain.util.dto;

import java.time.LocalDate;
import kr.allcll.backend.domain.util.SemesterCode;

public record SemesterResponse(
    String code,
    String semester,
    Period period
) {

    public static SemesterResponse from(SemesterCode semesterCode) {
        return new SemesterResponse(
            semesterCode.name(),
            semesterCode.getValue(),
            new Period(semesterCode.getStartDate(), semesterCode.getEndDate())
        );
    }

    public record Period(
        LocalDate startDate,
        LocalDate endDate
    ) {

    }
}
