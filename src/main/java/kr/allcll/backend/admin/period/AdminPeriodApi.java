package kr.allcll.backend.admin.period;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.period.dto.PeriodRequest;
import kr.allcll.backend.domain.period.OperationType;
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
public class AdminPeriodApi {

    private final AdminPeriodService periodService;
    private final AdminRequestValidator validator;

    @PostMapping("/api/admin/service-period")
    public ResponseEntity<Void> savePeriod(HttpServletRequest request,
        @RequestParam Semester semester,
        @RequestBody PeriodRequest periodRequest
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        periodService.savePeriod(semester, periodRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/admin/service-period")
    public ResponseEntity<Void> deletePeriod(HttpServletRequest request,
        @RequestParam Semester semester,
        @RequestParam OperationType operationType
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        periodService.deletePeriod(semester, operationType);
        return ResponseEntity.ok().build();
    }
}
