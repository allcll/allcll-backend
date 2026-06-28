package kr.allcll.backend.admin.seat;

import kr.allcll.backend.admin.seat.dto.SeatStatusResponse;
import kr.allcll.backend.support.batch.BatchService;
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
    private final BatchService batchService;
    private final TargetSubjectService targetSubjectService;

    @PostMapping("/api/admin/seat/start")
    public ResponseEntity<Void> getSeatPeriodically(@RequestParam(required = false) String userId) {
        targetSubjectService.loadGeneralSubjects();
        adminSeatService.getAllSeatPeriodically(userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/admin/season-seat/start")
    public ResponseEntity<Void> seasonSeatStart(@RequestParam(required = false) String userId) {
        targetSubjectService.loadAllSubjects();
        adminSeatService.getSeasonSeatPeriodically(userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/seat/check")
    public ResponseEntity<SeatStatusResponse> getSeatStatus() {
        SeatStatusResponse seatStatusResponse = adminSeatService.getSeatCrawlerStatus();
        return ResponseEntity.ok(seatStatusResponse);
    }

    @PostMapping("/api/admin/seat/cancel")
    public ResponseEntity<Void> cancelSeatScheduling() {
        adminSeatService.cancelSeatScheduling();
        batchService.flushAllBatch();

        return ResponseEntity.ok().build();
    }
}
