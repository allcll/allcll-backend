package kr.allcll.backend.domain.graduation.certification;

import jakarta.persistence.*;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "english_cert_criteria")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnglishCertCriterion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear; // 입학년도

    @Column(name = "admission_year_short", nullable = false)
    private Integer admissionYearShort;

    @Enumerated(EnumType.STRING)
    @Column(name = "english_target_type", nullable = false)
    private EnglishTargetType englishTargetType; // 전공자 구분

    @Column(name = "toeic_min_score", nullable = false)
    private Integer toeicMinScore; // TOEIC 최소 점수

    @Column(name = "toefl_ibt_min_score", nullable = false)
    private Integer toeflIbtMinScore; // TOEFL iBT 최소 점수

    @Column(name = "teps_min_score", nullable = false)
    private Integer tepsMinScore; // TEPS 최소 점수

    @Column(name = "new_teps_min_score", nullable = false)
    private Integer newTepsMinScore; // New TEPS 최소 점수

    @Column(name = "opic_min_level", nullable = false)
    private String opicMinLevel; // OPIc 최소 레벨

    @Column(name = "toeic_speaking_min_level", nullable = false)
    private String toeicSpeakingMinLevel; // TOEIC Speaking 최소 레벨

    @Column(name = "gtelp_level", nullable = false)
    private Integer gtelpLevel; // G-TELP 레벨

    @Column(name = "gtelp_min_score", nullable = false)
    private Integer gtelpMinScore; // G-TELP 최소 점수

    @Column(name = "gtelp_speaking_level", nullable = false)
    private Integer gtelpSpeakingLevel; // G-TELP Speaking 레벨

    @Column(name = "alt_curi_no")
    private String altCuriNo; // 대체 과목 학수번호

    @Column(name = "alt_curi_nm")
    private String altCuriNm; // 대체 과목명

    @Column(name = "alt_curi_credit")
    private Integer altCuriCredit; // 대체 과목 학점

    @Column(name = "note")
    private String note; // 비고

    public EnglishCertCriterion(
        Integer admissionYear,
        Integer admissionYearShort,
        EnglishTargetType englishTargetType,
        Integer toeicMinScore,
        Integer toeflIbtMinScore,
        Integer tepsMinScore,
        Integer newTepsMinScore,
        String opicMinLevel,
        String toeicSpeakingMinLevel,
        Integer gtelpLevel,
        Integer gtelpMinScore,
        Integer gtelpSpeakingLevel,
        String altCuriNo,
        String altCuriNm,
        Integer altCuriCredit,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.englishTargetType = englishTargetType;
        this.toeicMinScore = toeicMinScore;
        this.toeflIbtMinScore = toeflIbtMinScore;
        this.tepsMinScore = tepsMinScore;
        this.newTepsMinScore = newTepsMinScore;
        this.opicMinLevel = opicMinLevel;
        this.toeicSpeakingMinLevel = toeicSpeakingMinLevel;
        this.gtelpLevel = gtelpLevel;
        this.gtelpMinScore = gtelpMinScore;
        this.gtelpSpeakingLevel = gtelpSpeakingLevel;
        this.altCuriNo = altCuriNo;
        this.altCuriNm = altCuriNm;
        this.altCuriCredit = altCuriCredit;
        this.note = note;
    }
}
