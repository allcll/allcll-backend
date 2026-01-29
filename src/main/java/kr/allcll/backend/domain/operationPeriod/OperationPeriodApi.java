package kr.allcll.backend.domain.operationPeriod;

import java.time.LocalDate;
import kr.allcll.backend.domain.operationPeriod.dto.OperationPeriodsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OperationPeriodApi {

    private final OperationPeriodService operationPeriodService;

    @GetMapping("/api/operation-period")
    public ResponseEntity<OperationPeriodsResponse> findAll(@RequestParam LocalDate date) {
        OperationPeriodsResponse operationPeriodsResponse = operationPeriodService.findAll(date);
        return ResponseEntity.ok(operationPeriodsResponse);
    }
}
