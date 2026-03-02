package kr.allcll.backend.admin.review;

import java.util.List;
import kr.allcll.backend.admin.review.dto.AdminUserReviewResponse;
import kr.allcll.backend.admin.review.dto.AdminUserReviewResponses;
import kr.allcll.backend.domain.review.UserReview;
import kr.allcll.backend.domain.review.UserReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminUserReviewService {

    private final UserReviewRepository userReviewRepository;

    public AdminUserReviewResponses getReview() {
        List<UserReview> reviews = userReviewRepository.findAll();
        return new AdminUserReviewResponses(
            reviews.stream()
                .map(AdminUserReviewResponse::from)
                .toList()
        );
    }
}
