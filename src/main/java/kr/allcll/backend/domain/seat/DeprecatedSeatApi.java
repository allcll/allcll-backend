package kr.allcll.backend.domain.seat;

import java.util.List;
import kr.allcll.backend.admin.subject.TargetSubjectService;
import kr.allcll.backend.admin.seat.dto.PinSubjectUpdateRequest;
import kr.allcll.backend.domain.seat.dto.DeprecatedSubjectSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeprecatedSeatApi {

    private final TargetSubjectService targetSubjectService;
    private final DeprecatedSeatService deprecatedSeatService;

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
    public ResponseEntity<List<DeprecatedSubjectSummaryResponse>> getTargetSubjects() {
        List<DeprecatedSubjectSummaryResponse> response = deprecatedSeatService.getAllTargetSubjects();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/admin/target-general-subjects")
    public ResponseEntity<List<DeprecatedSubjectSummaryResponse>> getTargetGeneralSubjects() {
        List<DeprecatedSubjectSummaryResponse> response = deprecatedSeatService.getAllTargetGeneralSubjects();
        return ResponseEntity.ok(response);
    }
}
