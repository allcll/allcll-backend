package kr.allcll.backend.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController {

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
}
