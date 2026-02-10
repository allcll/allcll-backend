package kr.allcll.backend.domain.graduation.check.result;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "graduation_check")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraduationCheck extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "can_graduate", nullable = false)
    private Boolean canGraduate;

    @Column(name = "total_credits", nullable = false)
    private Double totalCredits;

    @Column(name = "required_total_credits", nullable = false)
    private Integer requiredTotalCredits;

    @Column(name = "remaining_credits", nullable = false)
    private Double remainingCredits;

    public GraduationCheck(
        Long userId,
        Boolean canGraduate,
        Double totalCredits,
        Integer requiredTotalCredits,
        Double remainingCredits
    ) {
        this.userId = userId;
        this.canGraduate = canGraduate;
        this.totalCredits = totalCredits;
        this.requiredTotalCredits = requiredTotalCredits;
        this.remainingCredits = remainingCredits;
    }

    public void update(
        Boolean canGraduate,
        Double totalCredits,
        Integer requiredTotalCredits,
        Double remainingCredits
    ) {
        this.canGraduate = canGraduate;
        this.totalCredits = totalCredits;
        this.requiredTotalCredits = requiredTotalCredits;
        this.remainingCredits = remainingCredits;
    }
}