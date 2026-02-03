package kr.allcll.backend.domain.graduation.check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "graduation_check_category_result",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "category_type"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraduationCheckCategoryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 사용자 id

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private CategoryType categoryType;  // 이수구분

    @Column(name = "earned_credits", nullable = false)
    private Integer earnedCredits;  // 이수구분 별 이수한 학점

    @Column(name = "required_credits", nullable = false)
    private Integer requiredCredits;  // 이수구분 별 필요 학점

    @Column(name = "remaining_credits", nullable = false)
    private Integer remainingCredits;  // 남은 학점

    @Column(name = "is_satisfied", nullable = false)
    private Boolean isSatisfied;  // 충족 여부

    public GraduationCheckCategoryResult(Long userId, CategoryType categoryType, Integer earnedCredits,
        Integer requiredCredits, Integer remainingCredits, Boolean isSatisfied) {
        this.userId = userId;
        this.categoryType = categoryType;
        this.earnedCredits = earnedCredits;
        this.requiredCredits = requiredCredits;
        this.remainingCredits = remainingCredits;
        this.isSatisfied = isSatisfied;
    }
}