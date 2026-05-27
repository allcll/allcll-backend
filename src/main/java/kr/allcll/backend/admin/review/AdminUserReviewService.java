package kr.allcll.backend.admin.review;

import java.util.List;
import kr.allcll.backend.admin.review.dto.AdminUserReviewsResponse;
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

    public AdminUserReviewsResponse getReview() {
        List<UserReview> reviews = userReviewRepository.findAll();
        return AdminUserReviewsResponse.from(reviews);
    }
}
