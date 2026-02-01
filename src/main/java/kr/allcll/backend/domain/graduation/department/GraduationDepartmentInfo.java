package kr.allcll.backend.domain.graduation.department;

import jakarta.persistence.*;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "graduation_department_info")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraduationDepartmentInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear; // 입학년도

    @Column(name = "admission_year_short", nullable = false)
    private Integer admissionYearShort;

    @Column(name = "dept_nm", nullable = false)
    private String deptNm; // 학과명

    @Column(name = "dept_cd")
    private String deptCd; // 학과코드

    @Column(name = "college_nm", nullable = false)
    private String collegeNm; // 단과대명

    @Enumerated(EnumType.STRING)
    @Column(name = "dept_group", nullable = false)
    private DeptGroup deptGroup; // 계열

    @Enumerated(EnumType.STRING)
    @Column(name = "english_target_type", nullable = false)
    private EnglishTargetType englishTargetType; // 영어인증 대상 여부

    @Enumerated(EnumType.STRING)
    @Column(name = "coding_target_type", nullable = false)
    private CodingTargetType codingTargetType; // 코딩인증 대상 여부

    @Column(name = "note")
    private String note; // 비고

    public GraduationDepartmentInfo(
        Integer admissionYear,
        Integer admissionYearShort,
        String deptNm,
        String deptCd,
        String collegeNm,
        DeptGroup deptGroup,
        EnglishTargetType englishTargetType,
        CodingTargetType codingTargetType,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.deptNm = deptNm;
        this.deptCd = deptCd;
        this.collegeNm = collegeNm;
        this.deptGroup = deptGroup;
        this.englishTargetType = englishTargetType;
        this.codingTargetType = codingTargetType;
        this.note = note;
    }
}

