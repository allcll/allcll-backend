package kr.allcll.backend.admin.operationPeriod;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.operationPeriod.dto.OperationPeriodRequest;
import kr.allcll.backend.domain.operationPeriod.OperationType;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/operation-period")
@RequiredArgsConstructor
public class AdminOperationPeriodApi {

    private final AdminOperationPeriodService operationPeriodService;
    private final AdminRequestValidator validator;

    @PostMapping
    public ResponseEntity<Void> saveOperationPeriod(HttpServletRequest request,
        @RequestParam Semester semester,
        @RequestBody OperationPeriodRequest operationPeriodRequest
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        operationPeriodService.saveOperationPeriod(semester, operationPeriodRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteOperationPeriod(HttpServletRequest request,
        @RequestParam Semester semester,
        @RequestParam OperationType operationType
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        operationPeriodService.deleteOperationPeriod(semester, operationType);
        return ResponseEntity.ok().build();
    }
}
