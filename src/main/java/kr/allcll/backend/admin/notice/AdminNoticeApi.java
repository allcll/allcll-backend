package kr.allcll.backend.admin.notice;

import jakarta.validation.Valid;
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

    @GetMapping("/api/admin/notices")
    public ResponseEntity<AdminNoticesResponse> getAllNotice() {
        AdminNoticesResponse response = adminNoticeService.getAllNotice();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/admin/notices")
    public ResponseEntity<CreateNoticeResponse> createNotice(
        @Valid @RequestBody CreateNoticeRequest createNoticeRequest
    ) {
        CreateNoticeResponse response = adminNoticeService.createNewNotice(createNoticeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/api/admin/notices/{id}")
    public ResponseEntity<UpdateNoticeResponse> modifyNotice(
        @PathVariable Long id,
        @Valid @RequestBody UpdateNoticeRequest updateNoticeRequest
    ) {
        UpdateNoticeResponse response = adminNoticeService.updateNotice(id, updateNoticeRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/admin/notices/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        adminNoticeService.deleteNotice(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
