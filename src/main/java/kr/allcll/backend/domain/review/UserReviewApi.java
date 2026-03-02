package kr.allcll.backend.domain.review;

import kr.allcll.backend.domain.review.dto.UserReviewRequest;
import kr.allcll.backend.support.web.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserReviewApi {

    private final UserReviewService userReviewService;

    @PostMapping("api/review")
    public ResponseEntity<Void> createReview(
        @Auth Long userId,
        @RequestBody UserReviewRequest userReviewRequest
    ) {
        userReviewService.createReview(userId, userReviewRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
