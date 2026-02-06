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

    @Column(name = "alt_curi_no")
    private String altCuriNo; // 대체 과목 학수번호

    @Column(name = "alt_min_grade")
    private String altMinGrade; // 대체 과목 최소 성적

    @Column(name = "note")
    private String note; // 비고

    public CodingCertCriterion(
        Integer admissionYear,
        Integer admissionYearShort,
        CodingTargetType codingTargetType,
        Integer toscMinLevel,
        String altCuriNo,
        String altMinGrade,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.codingTargetType = codingTargetType;
        this.toscMinLevel = toscMinLevel;
        this.altCuriNo = altCuriNo;
        this.altMinGrade = altMinGrade;
        this.note = note;
    }
}
