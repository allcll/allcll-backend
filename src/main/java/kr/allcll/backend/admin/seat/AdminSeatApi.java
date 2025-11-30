package kr.allcll.backend.admin.seat;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
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
    private final SeatSchedulerTracker schedulerTracker;

    @PostMapping("/api/admin/seat/start")
    public ResponseEntity<Void> getSeatPeriodically(HttpServletRequest request,
        @RequestParam(required = false) String userId) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        schedulerTracker.addUserId(userId);

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
        schedulerTracker.addUserId(userId);

        targetSubjectService.loadAllSubjects();
        adminSeatService.getSeasonSeatPeriodically(userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/seat/check")
    public ResponseEntity<SeatStatusResponse> getSeatStatus(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        List<String> userIds = schedulerTracker.getUserIds();
        SeatStatusResponse seatStatusResponse = adminSeatService.getSeatCrawlerStatus(userIds);
        return ResponseEntity.ok(seatStatusResponse);
    }

    @PostMapping("/api/admin/seat/cancel")
    public ResponseEntity<Void> cancelSeatScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        adminSeatService.cancelSeatScheduling();

        schedulerTracker.clearAll();

        return ResponseEntity.ok().build();
    }
}
