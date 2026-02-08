package kr.allcll.backend.domain.graduation.certification;

import jakarta.persistence.*;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "coding_cert_criteria")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodingCertCriterion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear; // 입학년도

    @Column(name = "admission_year_short", nullable = false)
    private Integer admissionYearShort;

    @Enumerated(EnumType.STRING)
    @Column(name = "coding_target_type", nullable = false)
    private CodingTargetType codingTargetType; // 전공자 구분

    @Column(name = "tosc_min_level", nullable = false)
    private Integer toscMinLevel; // TOSC 최소 급수

    @Column(name = "alt1_curi_no")
    private String alt1CuriNo; // 대체 과목1 학수번호

    @Column(name = "alt1_curi_nm")
    private String alt1CuriNm; // 대체 과목1 학수번호

    @Column(name = "alt1_min_grade")
    private String alt1MinGrade; // 대체 과목1 최소 성적

    @Column(name = "alt2_curi_no")
    private String alt2CuriNo; // 대체 과목2 학수번호

    @Column(name = "alt2_curi_nm")
    private String alt2CuriNm; // 대체 과목1 학수번호

    @Column(name = "alt2_min_grade")
    private String alt2MinGrade; // 대체 과목2 최소 성적

    @Column(name = "note")
    private String note; // 비고

    public CodingCertCriterion(
        Integer admissionYear,
        Integer admissionYearShort,
        CodingTargetType codingTargetType,
        Integer toscMinLevel,
        String alt1CuriNo,
        String alt1CuriNm,
        String alt1MinGrade,
        String alt2CuriNo,
        String alt2CuriNm,
        String alt2MinGrade,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.codingTargetType = codingTargetType;
        this.toscMinLevel = toscMinLevel;
        this.alt1CuriNo = alt1CuriNo;
        this.alt1CuriNm = alt1CuriNm;
        this.alt1MinGrade = alt1MinGrade;
        this.alt2CuriNo = alt2CuriNo;
        this.alt2CuriNm = alt2CuriNm;
        this.alt2MinGrade = alt2MinGrade;
        this.note = note;
    }
}
