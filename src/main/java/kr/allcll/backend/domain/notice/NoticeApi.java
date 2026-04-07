package kr.allcll.backend.domain.notice;

import kr.allcll.backend.domain.notice.dto.NoticesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NoticeApi {

    private final NoticeService noticeService;

    @GetMapping("/api/notices")
    public ResponseEntity<NoticesResponse> getAllNotice() {
        NoticesResponse response = noticeService.getAllNotice();
        return ResponseEntity.ok(response);
    }
}
