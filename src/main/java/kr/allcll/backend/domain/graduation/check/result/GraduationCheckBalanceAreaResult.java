package kr.allcll.backend.domain.graduation.check.result;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "graduation_check_balance_area_result")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraduationCheckBalanceAreaResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "area", nullable = false)
    private BalanceRequiredArea balanceRequiredArea;

    public GraduationCheckBalanceAreaResult(Long userId, BalanceRequiredArea balanceRequiredArea) {
        this.userId = userId;
        this.balanceRequiredArea = balanceRequiredArea;
    }
}
