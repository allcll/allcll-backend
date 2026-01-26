package kr.allcll.backend.domain.period.dto;

import java.util.List;
import kr.allcll.backend.admin.period.dto.PeriodDetailResponse;
import kr.allcll.backend.domain.period.OperationPeriod;
import kr.allcll.backend.support.semester.Semester;

public record PeriodsResponse(
        Semester code,
        String semester,
        List<PeriodDetailResponse> periodDetailResponses
) {

    public static PeriodsResponse from(List<OperationPeriod> periods) {
        if (periods.isEmpty()) {
            return new PeriodsResponse(null, null, List.of());
        }

        return new PeriodsResponse(
                periods.getFirst().getSemester(),
                periods.getFirst().getSemesterValue(),
                periods.stream().map(PeriodDetailResponse::from).toList()
        );
    }
}
