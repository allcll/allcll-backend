package kr.allcll.backend.domain.graduation.check.cert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "graduation_check")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraduationCheck {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;  // 사용자 id

    @Column(name = "can_graduate", nullable = false)
    private Boolean canGraduate;  // 졸업 가능 여부

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits;  // 총 이수한 학점

    @Column(name = "required_total_credits", nullable = false)
    private Integer requiredTotalCredits;  // 총 이수해야하는 학점

    @Column(name = "remaining_credits", nullable = false)
    private Integer remainingCredits;  // 남은 학점

    public GraduationCheck(Long userId, Boolean canGraduate, Integer totalCredits,
        Integer requiredTotalCredits,
        Integer remainingCredits) {
        this.userId = userId;
        this.canGraduate = canGraduate;
        this.totalCredits = totalCredits;
        this.requiredTotalCredits = requiredTotalCredits;
        this.remainingCredits = remainingCredits;
    }
}