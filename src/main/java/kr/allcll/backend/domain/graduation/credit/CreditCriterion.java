package kr.allcll.backend.domain.graduation.credit;

import jakarta.persistence.*;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Table(name = "credit_criteria")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditCriterion extends BaseEntity {

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

    @Column(name = "dept_cd", nullable = false)
    private String deptCd; // 학과 코드

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

    public CreditCriterion(
        Integer admissionYear,
        Integer admissionYearShort,
        MajorType majorType,
        String deptCd,
        MajorScope majorScope,
        CategoryType categoryType,
        Integer requiredCredits,
        Boolean enabled,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.majorType = majorType;
        this.deptCd = deptCd;
        this.majorScope = majorScope;
        this.categoryType = categoryType;
        this.requiredCredits = requiredCredits;
        this.enabled = enabled;
        this.note = note;
    }
}
