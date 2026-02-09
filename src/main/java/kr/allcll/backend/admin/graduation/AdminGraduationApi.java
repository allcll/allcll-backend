package kr.allcll.backend.admin.graduation;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminGraduationApi {

    private final AdminRequestValidator validator;
    private final AdminGraduationSyncService adminGraduationSyncService;

    @PostMapping("/api/admin/graduation/sync")
    public ResponseEntity<Void> syncGraduationRules(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        adminGraduationSyncService.syncGraduationRules();
        return ResponseEntity.ok().build();
    }
}
