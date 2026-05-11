package kr.allcll.backend.admin.graduation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminGraduationApi {

    private final AdminGraduationSyncService adminGraduationSyncService;

    @PostMapping("/api/admin/graduation/sync")
    public ResponseEntity<Void> syncGraduationRules() {
        adminGraduationSyncService.syncGraduationRules();
        return ResponseEntity.ok().build();
    }
}
