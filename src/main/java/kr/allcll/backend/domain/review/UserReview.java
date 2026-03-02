package kr.allcll.backend.domain.review;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.allcll.backend.domain.operationPeriod.OperationType;
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

    public UserReview(String studentId, Short rate, OperationType operationType, String detail) {
        this.studentId = studentId;
        this.rate = rate;
        this.operationType = operationType;
        this.detail = detail;
    }
}
