package kr.allcll.backend.admin;

import java.util.List;
import kr.allcll.crawler.seat.CrawlerSeatService;
import kr.allcll.crawler.seat.PinSubjectUpdateRequest;
import kr.allcll.crawler.seat.SubjectSummaryResponse;
import kr.allcll.crawler.seat.TargetSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminSeatApi {

    private final CrawlerSeatService crawlerSeatService;
    private final TargetSubjectService targetSubjectService;

    @GetMapping("/api/seat/start")
    public ResponseEntity<Void> getSeatPeriodically(@RequestParam(required = false) String userId) {
        targetSubjectService.loadGeneralSubjects();
        crawlerSeatService.getAllSeatPeriodically(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/season-seat/start")
    public ResponseEntity<Void> seasonSeatStart(@RequestParam(required = false) String userId) {
        targetSubjectService.loadAllSubjects();
        crawlerSeatService.getSeasonSeatPeriodically(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/seat/cancel")
    public ResponseEntity<Void> cancelSeatScheduling() {
        crawlerSeatService.cancelSeatScheduling();
        return ResponseEntity.ok().build();
    }

    /**
     * deprecated: 백엔드와의 통신이 크롤러의 서브모듈화로 필요 없어 졌습니다. 따라서 해당 api를 통해 loadPinSubject를 하는 것이 아닌, 백엔드 서버에서 직접
     * loadPinSubjects를 호출합니다.
     */
    @PutMapping("/api/pin")
    public ResponseEntity<Void> pinSubject(@RequestBody PinSubjectUpdateRequest request) {
        targetSubjectService.loadPinSubjects(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/target-subjects")
    public ResponseEntity<List<SubjectSummaryResponse>> getTargetSubjects() {
        List<SubjectSummaryResponse> response = targetSubjectService.getAllTargetSubjects();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/admin/target-general-subjects")
    public ResponseEntity<List<SubjectSummaryResponse>> getTargetGeneralSubjects() {
        List<SubjectSummaryResponse> response = targetSubjectService.getAllTargetGeneralSubjects();
        return ResponseEntity.ok(response);
    }
}