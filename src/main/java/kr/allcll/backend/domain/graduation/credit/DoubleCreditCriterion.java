package kr.allcll.backend.domain.graduation.credit;

import jakarta.persistence.*;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "double_credit_criteria")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DoubleCreditCriterion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear; // 입학년도

    @Column(name = "admission_year_short", nullable = false)
    private Integer admissionYearShort;

    @Enumerated(EnumType.STRING)
    @Column(name = "major_type", nullable = false)
    private MajorType majorType; // 전공 이수 형태

    @Column(name = "primary_dept_cd", nullable = false)
    private String primaryDeptCd; // 주전공 학과 코드

    @Column(name = "primary_dept_nm", nullable = false)
    private String primaryDeptNm; // 주전공 학과명

    @Column(name = "secondary_dept_cd", nullable = false)
    private String secondaryDeptCd; // 복수전공 학과 코드

    @Column(name = "secondary_dept_nm", nullable = false)
    private String secondaryDeptNm; // 복수전공 학과명

    @Enumerated(EnumType.STRING)
    @Column(name = "major_scope", nullable = false)
    private MajorScope majorScope; // 기준이 적용되는 전공 형태

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private CategoryType categoryType; // 이수구분

    @Column(name = "required_credits", nullable = false)
    private Integer requiredCredits; // 최소 요구 학점

    @Column(name = "enabled", nullable = false)
    private Boolean enabled; // 검사 적용 여부

    @Column(name = "note")
    private String note; // 비고

    public DoubleCreditCriterion(
        Integer admissionYear,
        Integer admissionYearShort,
        MajorType majorType,
        String primaryDeptCd,
        String primaryDeptNm,
        String secondaryDeptCd,
        String secondaryDeptNm,
        MajorScope majorScope,
        CategoryType categoryType,
        Integer requiredCredits,
        Boolean enabled,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.majorType = majorType;
        this.primaryDeptCd = primaryDeptCd;
        this.primaryDeptNm = primaryDeptNm;
        this.secondaryDeptCd = secondaryDeptCd;
        this.secondaryDeptNm = secondaryDeptNm;
        this.majorScope = majorScope;
        this.categoryType = categoryType;
        this.requiredCredits = requiredCredits;
        this.enabled = enabled;
        this.note = note;
    }
}
