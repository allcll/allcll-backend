package kr.allcll.backend.domain.review;

import jakarta.persistence.Entity;
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

    private OperationType operationType;

    private String detail;

    public UserReview(String studentId, Short rate, OperationType operationType, String detail) {
        this.studentId = studentId;
        this.rate = rate;
        this.operationType = operationType;
        this.detail = detail;
    }

    public UserReview(User user, UserReviewRequest userReviewRequest) {
        this.studentId = user.getStudentId();
        this.rate = userReviewRequest.rate();
        this.operationType = OperationType.from(userReviewRequest.operationType());
        this.detail = userReviewRequest.detail();
    }
}
