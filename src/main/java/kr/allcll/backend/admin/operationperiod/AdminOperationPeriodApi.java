package kr.allcll.backend.admin.operationperiod;

import kr.allcll.backend.admin.operationperiod.dto.OperationPeriodRequest;
import kr.allcll.backend.domain.operationperiod.OperationType;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminOperationPeriodApi {

    private final AdminOperationPeriodService operationPeriodService;

    @PostMapping("/api/admin/operation-period")
    public ResponseEntity<Void> saveOperationPeriod(
        @RequestParam("semesterCode") Semester semester,
        @RequestBody OperationPeriodRequest operationPeriodRequest
    ) {
        operationPeriodService.saveOperationPeriod(semester, operationPeriodRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/admin/operation-period")
    public ResponseEntity<Void> deleteOperationPeriod(
        @RequestParam("semesterCode") Semester semester,
        @RequestParam OperationType operationType
    ) {
        operationPeriodService.deleteOperationPeriod(semester, operationType);
        return ResponseEntity.ok().build();
    }
}
