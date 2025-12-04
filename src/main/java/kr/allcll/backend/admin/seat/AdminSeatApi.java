package kr.allcll.backend.admin.seat;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.seat.dto.SeatStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminSeatApi {

    private final AdminSeatService adminSeatService;
    private final TargetSubjectService targetSubjectService;
    private final AdminRequestValidator validator;

    @PostMapping("/api/admin/seat/start")
    public ResponseEntity<Void> getSeatPeriodically(HttpServletRequest request,
        @RequestParam(required = false) String userId) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }

        targetSubjectService.loadGeneralSubjects();
        adminSeatService.getAllSeatPeriodically(userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/admin/season-seat/start")
    public ResponseEntity<Void> seasonSeatStart(HttpServletRequest request,
        @RequestParam(required = false) String userId) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }

        targetSubjectService.loadAllSubjects();
        adminSeatService.getSeasonSeatPeriodically(userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/seat/check")
    public ResponseEntity<SeatStatusResponse> getSeatStatus(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        SeatStatusResponse seatStatusResponse = adminSeatService.getSeatCrawlerStatus();
        return ResponseEntity.ok(seatStatusResponse);
    }

    @PostMapping("/api/admin/seat/cancel")
    public ResponseEntity<Void> cancelSeatScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        adminSeatService.cancelSeatScheduling();

        return ResponseEntity.ok().build();
    }
}
