package kr.allcll.backend.domain.period;

import kr.allcll.backend.domain.period.dto.PeriodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PeriodApi {

    private final PeriodService periodService;

    @GetMapping("/api/service-period")
    public ResponseEntity<PeriodResponse> getPeriod() {
        return ResponseEntity.ok(periodService.getPeriod());
    }
}
