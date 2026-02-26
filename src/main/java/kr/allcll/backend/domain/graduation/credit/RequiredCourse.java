package kr.allcll.backend.domain.graduation.credit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "required_courses")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequiredCourse extends BaseEntity {

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
    private String deptNm; // 학과 코드

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private CategoryType categoryType; // 이수구분

    @Column(name = "curi_no", nullable = false)
    private String curiNo; // 학수번호

    @Column(name = "curi_nm", nullable = false, length = 255)
    private String curiNm; // 과목명

    @Column(name = "alt_group")
    private String altGroup; // 선택 과목 그룹 키

    @Column(name = "required", nullable = false)
    private Boolean required; // 검사 대상 여부

    @Column(name = "note", length = 255)
    private String note; // 비고

    public RequiredCourse(
        Integer admissionYear,
        Integer admissionYearShort,
        String deptCd,
        String deptNm,
        CategoryType categoryType,
        String curiNo,
        String curiNm,
        String altGroup,
        Boolean required,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.deptCd = deptCd;
        this.deptNm = deptNm;
        this.categoryType = categoryType;
        this.curiNo = curiNo;
        this.curiNm = curiNm;
        this.altGroup = altGroup;
        this.required = required;
        this.note = note;
    }
}

