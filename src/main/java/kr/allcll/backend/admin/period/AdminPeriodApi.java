package kr.allcll.backend.admin.period;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.period.dto.PeriodRequest;
import kr.allcll.backend.support.semester.Semester;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> createPeriod(HttpServletRequest request,
        @RequestParam Semester semesterCode,
        @RequestBody PeriodRequest periodRequest) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        periodService.createPeriod(semesterCode, periodRequest);
        return ResponseEntity.ok().build();
    }
}
