package kr.allcll.backend.domain.period.dto;

import java.util.List;
import kr.allcll.backend.domain.period.OperationPeriod;
import kr.allcll.backend.support.semester.Semester;

public record PeriodsResponse(
    Semester code,
    String semester,
    List<PeriodDetailResponse> periodDetailResponses
) {

    public static PeriodsResponse from(List<OperationPeriod> operationPeriods) {
        if (operationPeriods.isEmpty()) {
            return new PeriodsResponse(null, null, List.of());
        }

        return new PeriodsResponse(
            operationPeriods.getFirst().getSemester(),
            operationPeriods.getFirst().getSemesterValue(),
            operationPeriods.stream()
                .map(PeriodDetailResponse::from)
                .toList()
        );
    }
}
