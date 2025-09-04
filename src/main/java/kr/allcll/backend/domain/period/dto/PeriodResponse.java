package kr.allcll.backend.domain.period.dto;

import java.util.List;
import kr.allcll.backend.domain.period.Period;
import kr.allcll.backend.support.semester.Semester;

public record PeriodResponse(
    Semester code,
    String semester,
    List<PeriodDetailRequest> services
) {

    public static PeriodResponse from(List<Period> periods) {
        if (periods.isEmpty()) {
            return new PeriodResponse(null, null, List.of());
        }

        return new PeriodResponse(
            periods.get(0).getSemesterCode(),
            periods.get(0).getSemesterValue(),
            periods.stream().map(PeriodDetailRequest::from).toList()
        );
    }
}
