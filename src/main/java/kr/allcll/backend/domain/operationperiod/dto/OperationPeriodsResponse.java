package kr.allcll.backend.domain.operationperiod.dto;

import java.util.List;
import kr.allcll.backend.domain.operationperiod.OperationPeriod;
import kr.allcll.backend.support.semester.Semester;

public record OperationPeriodsResponse(
    Semester semesterCode,
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
