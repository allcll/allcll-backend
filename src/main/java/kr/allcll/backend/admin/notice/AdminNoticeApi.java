package kr.allcll.backend.admin.notice;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.notice.dto.CreateNoticeRequest;
import kr.allcll.backend.admin.notice.dto.CreateNoticeResponse;
import kr.allcll.backend.admin.notice.dto.NoticesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminNoticeApi {

    private final AdminNoticeService adminNoticeService;
    private final AdminRequestValidator validator;

    @GetMapping("/api/admin/notices")
    public ResponseEntity<NoticesResponse> getAllNotice(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        NoticesResponse response = adminNoticeService.getAllNotice();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/admin/notices")
    public ResponseEntity<CreateNoticeResponse> createNotice(
        HttpServletRequest request,
        @RequestBody CreateNoticeRequest createNoticeRequest
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        CreateNoticeResponse response = adminNoticeService.createNewNotice(createNoticeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
