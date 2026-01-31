package kr.allcll.backend.domain.graduation.balance;

import jakarta.persistence.*;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "balance_required_area_exclusions")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalanceRequiredAreaExclusion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear; // 입학년도

    @Column(name = "admission_year_short", nullable = false)
    private Integer admissionYearShort;

    @Enumerated(EnumType.STRING)
    @Column(name = "dept_group", nullable = false)
    private DeptGroup deptGroup; // 계열

    @Enumerated(EnumType.STRING)
    @Column(name = "excluded_domain_type", nullable = false)
    private BalanceRequiredArea excludedBalanceRequiredArea; // 제외 영역

    public BalanceRequiredAreaExclusion(
        Integer admissionYear,
        Integer admissionYearShort,
        DeptGroup deptGroup,
        BalanceRequiredArea excludedBalanceRequiredArea
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.deptGroup = deptGroup;
        this.excludedBalanceRequiredArea = excludedBalanceRequiredArea;
    }
}

