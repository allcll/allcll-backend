package kr.allcll.backend.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminApi {

    private final AdminService adminService;
    private final AdminRequestValidator validator;

    @PostMapping("/api/admin/scheduler/start")
    public ResponseEntity<Void> startScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        adminService.startScheduling();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/admin/scheduler/cancel")
    public ResponseEntity<Void> cancelScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        adminService.cancelScheduling();
        return ResponseEntity.ok().build();
    }
}
