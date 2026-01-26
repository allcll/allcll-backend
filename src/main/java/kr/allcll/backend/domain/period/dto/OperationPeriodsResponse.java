package kr.allcll.backend.domain.period.dto;

import java.util.List;
import kr.allcll.backend.domain.period.OperationPeriod;
import kr.allcll.backend.support.semester.Semester;

public record OperationPeriodsResponse(
    Semester semesterName,
    String semesterKoreanName,
    List<OperationPeriodDetailResponse> operationPeriodDetailResponses
) {

    public static OperationPeriodsResponse from(List<OperationPeriod> operationPeriods) {
        if (operationPeriods.isEmpty()) {
            return new OperationPeriodsResponse(null, null, List.of());
        }

        return new OperationPeriodsResponse(
            operationPeriods.getFirst().getSemester(),
            operationPeriods.getFirst().getSemesterKoreanName(),
            operationPeriods.stream()
                .map(OperationPeriodDetailResponse::from)
                .toList()
        );
    }
}
