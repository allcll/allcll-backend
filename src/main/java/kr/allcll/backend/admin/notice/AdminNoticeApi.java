package kr.allcll.backend.admin.notice;

import kr.allcll.backend.admin.notice.dto.NoticesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminNoticeApi {

    private final AdminAnnouncementService adminAnnouncementService;

    @GetMapping("/api/admin/notices")
    public ResponseEntity<NoticesResponse> getAllNotice() {
        NoticesResponse response = adminAnnouncementService.getAllNotice();
        return ResponseEntity.ok(response);
    }
}
