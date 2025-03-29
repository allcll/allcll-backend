package kr.allcll.backend.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminApi {

    private final AdminService adminService;

    @PostMapping("/api/admin/scheduler/start")
    public ResponseEntity<Void> startScheduling() {
        adminService.startScheduling();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/admin/scheduler/cancel")
    public ResponseEntity<Void> cancelScheduling() {
        adminService.cancelScheduling();
        return ResponseEntity.ok().build();
    }
}
