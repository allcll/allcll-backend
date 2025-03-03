package kr.allcll.backend.admin;

import kr.allcll.backend.admin.dto.SystemStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminApi {

    private final AdminService adminService;

    @PostMapping("/admin/sse-connect")
    public ResponseEntity<Void> sseConnect() {
        adminService.sseConnect();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/sse-disconnect")
    public ResponseEntity<Void> sseDisconnect() {
        adminService.sseDisconnect();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/send-non-major")
    public ResponseEntity<Void> startToSendNonMajor() {
        adminService.startToSendNonMajor();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/system-status")
    public ResponseEntity<SystemStatusResponse> getInitialStatus() {
        SystemStatusResponse response = adminService.getInitialStatus();
        return ResponseEntity.ok(response);
    }
}
