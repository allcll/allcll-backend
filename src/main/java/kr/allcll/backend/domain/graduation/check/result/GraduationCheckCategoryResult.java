package kr.allcll.backend.domain.graduation.check.result;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
    name = "graduation_check_category_result",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "major_scope", "category_type"})
    }
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraduationCheckCategoryResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "major_scope", nullable = false)
    private MajorScope majorScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private CategoryType categoryType;

    @Column(name = "my_credits", nullable = false)
    private Double myCredits;

    @Column(name = "required_credits", nullable = false)
    private Integer requiredCredits;

    @Column(name = "remaining_credits", nullable = false)
    private Double remainingCredits;

    @Column(name = "is_satisfied", nullable = false)
    private Boolean isSatisfied;

    public GraduationCheckCategoryResult(
        Long userId,
        MajorScope majorScope,
        CategoryType categoryType,
        Double myCredits,
        Integer requiredCredits,
        Double remainingCredits,
        Boolean isSatisfied
    ) {
        this.userId = userId;
        this.majorScope = majorScope;
        this.categoryType = categoryType;
        this.myCredits = myCredits;
        this.requiredCredits = requiredCredits;
        this.remainingCredits = remainingCredits;
        this.isSatisfied = isSatisfied;
    }

    public void update(
        Double earnedCredits,
        Integer requiredCredits,
        Double remainingCredits,
        Boolean isSatisfied
    ) {
        this.myCredits = earnedCredits;
        this.requiredCredits = requiredCredits;
        this.remainingCredits = remainingCredits;
        this.isSatisfied = isSatisfied;
    }
}
