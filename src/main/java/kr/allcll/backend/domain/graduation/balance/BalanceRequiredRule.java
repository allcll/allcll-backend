package kr.allcll.backend.domain.graduation.balance;

import jakarta.persistence.*;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "balance_required_rules")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalanceRequiredRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear; // 입학년도

    @Column(name = "admission_year_short", nullable = false)
    private Integer admissionYearShort;

    @Column(name = "dept_cd", nullable = false)
    private String deptCd; // 학과 코드

    @Column(name = "dept_nm", nullable = false)
    private String deptNm; // 학과명

    @Column(name = "required", nullable = false)
    private Boolean required; // 적용 여부

    @Column(name = "required_areas_cnt", nullable = false)
    private Integer requiredAreasCnt; // 요구 영역 개수

    @Column(name = "required_credits", nullable = false)
    private Integer requiredCredits; // 요구 총 학점

    @Column(name = "note")
    private String note; // 비고

    public BalanceRequiredRule(
        Integer admissionYear,
        Integer admissionYearShort,
        String deptCd,
        String deptNm,
        Boolean isRequired,
        Integer requiredAreasCnt,
        Integer requiredCredits,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.deptCd = deptCd;
        this.deptNm = deptNm;
        this.required = isRequired;
        this.requiredAreasCnt = requiredAreasCnt;
        this.requiredCredits = requiredCredits;
        this.note = note;
    }
}
