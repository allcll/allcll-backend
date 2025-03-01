package kr.allcll.backend.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/admin/set-sse-connection-open")
    public ResponseEntity<Void> sseConnect() {
        adminService.sseConnect();
        return ResponseEntity.ok().build();
    }
}
