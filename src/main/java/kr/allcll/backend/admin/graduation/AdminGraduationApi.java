package kr.allcll.backend.admin.graduation;

import kr.allcll.backend.admin.graduation.dto.GraduationDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminGraduationApi {

    private final AdminGraduationSyncService adminGraduationSyncService;
    private final AdminGraduationService adminGraduationService;

    @PostMapping("/api/admin/graduation/sync")
    public ResponseEntity<Void> syncGraduationRules() {
        adminGraduationSyncService.syncGraduationRules();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/graduation/{studentId}")
    public ResponseEntity<GraduationDetailResponse> getGraduationDetail(@PathVariable String studentId) {
        GraduationDetailResponse graduationDetailResponse = adminGraduationService.getGraduationDetail(studentId);
        return ResponseEntity.ok(graduationDetailResponse);
    }
}
