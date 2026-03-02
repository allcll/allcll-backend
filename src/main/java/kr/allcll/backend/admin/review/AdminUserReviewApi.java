package kr.allcll.backend.admin.review;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.review.dto.AdminUserReviewResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminUserReviewApi {

    private final AdminRequestValidator validator;
    private final AdminUserReviewService adminUserReviewService;

    @GetMapping("/api/admin/review")
    public ResponseEntity<AdminUserReviewResponses> getReview(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        AdminUserReviewResponses responses = adminUserReviewService.getReview();
        return ResponseEntity.ok(responses);
    }
}
