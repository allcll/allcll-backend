package kr.allcll.backend.admin.sse;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.support.scheduler.SchedulerService;
import kr.allcll.backend.support.scheduler.dto.SeatSchedulerStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminSseApi {

    private final SchedulerService schedulerService;
    private final AdminRequestValidator validator;

    @PostMapping("/api/admin/seat-scheduler/start")
    public ResponseEntity<Void> startScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        schedulerService.startScheduling();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/admin/seat-scheduler/cancel")
    public ResponseEntity<Void> cancelScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        schedulerService.cancelScheduling();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/seat-scheduler/check")
    public ResponseEntity<SeatSchedulerStatusResponse> checkSchedulerStatus(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        SeatSchedulerStatusResponse seatSchedulerStatusResponse = schedulerService.getSeatSchedulerStatus();
        return ResponseEntity.ok(seatSchedulerStatusResponse);
    }
}
