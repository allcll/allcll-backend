package kr.allcll.backend.domain.period;

import java.time.LocalDate;
import kr.allcll.backend.domain.period.dto.OperationPeriodsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PeriodApi {

    private final PeriodService periodService;

    @GetMapping("/api/service-period")
    public ResponseEntity<OperationPeriodsResponse> findAll(@RequestParam LocalDate date) {
        OperationPeriodsResponse operationPeriodsResponse = periodService.findAll(date);
        return ResponseEntity.ok(operationPeriodsResponse);
    }
}
