package kr.allcll.backend.admin.review;

import kr.allcll.backend.admin.review.dto.AdminUserReviewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminUserReviewApi {

    private final AdminUserReviewService adminUserReviewService;

    @GetMapping("/api/admin/review")
    public ResponseEntity<AdminUserReviewsResponse> getReview() {
        AdminUserReviewsResponse responses = adminUserReviewService.getReview();
        return ResponseEntity.ok(responses);
    }
}
