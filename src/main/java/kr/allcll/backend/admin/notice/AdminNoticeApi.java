package kr.allcll.backend.admin.notice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.notice.dto.CreateNoticeRequest;
import kr.allcll.backend.admin.notice.dto.CreateNoticeResponse;
import kr.allcll.backend.admin.notice.dto.AdminNoticesResponse;
import kr.allcll.backend.admin.notice.dto.UpdateNoticeRequest;
import kr.allcll.backend.admin.notice.dto.UpdateNoticeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminNoticeApi {

    private final AdminNoticeService adminNoticeService;
    private final AdminRequestValidator validator;

    @GetMapping("/api/admin/notices")
    public ResponseEntity<AdminNoticesResponse> getAllNotice(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        AdminNoticesResponse response = adminNoticeService.getAllNotice();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/admin/notices")
    public ResponseEntity<CreateNoticeResponse> createNotice(
        HttpServletRequest request,
        @Valid @RequestBody CreateNoticeRequest createNoticeRequest
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        CreateNoticeResponse response = adminNoticeService.createNewNotice(createNoticeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/api/admin/notices/{id}")
    public ResponseEntity<UpdateNoticeResponse> modifyNotice(
        HttpServletRequest request,
        @PathVariable Long id,
        @Valid @RequestBody UpdateNoticeRequest updateNoticeRequest
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        UpdateNoticeResponse response = adminNoticeService.updateNotice(id, updateNoticeRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/admin/notices/{id}")
    public ResponseEntity<Void> deleteNotice(
        HttpServletRequest request,
        @PathVariable Long id
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        adminNoticeService.deleteNotice(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
