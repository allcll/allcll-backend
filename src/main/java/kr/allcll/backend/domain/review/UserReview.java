package kr.allcll.backend.domain.review;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.allcll.backend.domain.operationPeriod.OperationType;
import kr.allcll.backend.domain.review.dto.UserReviewRequest;
import kr.allcll.backend.domain.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentId;

    private Short rate;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    private String detail;

    public UserReview(User user, UserReviewRequest userReviewRequest) {
        this.studentId = user.getStudentId();
        this.rate = userReviewRequest.rate();
        this.operationType = userReviewRequest.operationType();
        this.detail = userReviewRequest.detail();
    }
}
