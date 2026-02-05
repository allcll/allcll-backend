package kr.allcll.backend.domain.graduation.certification;

import jakarta.persistence.*;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "classic_cert_criteria")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassicCertCriterion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear; // 입학년도

    @Column(name = "admission_year_short", nullable = false)
    private Integer admissionYearShort;

    @Column(name = "total_required_count", nullable = false)
    private Integer totalRequiredCount; // 총 요구 권수

    @Column(name = "required_count_western", nullable = false)
    private Integer requiredCountWestern; // 서양 영역 요구 권수

    @Column(name = "required_count_eastern", nullable = false)
    private Integer requiredCountEastern; // 동양 영역 요구 권수

    @Column(name = "required_count_eastern_and_western", nullable = false)
    private Integer requiredCountEasternAndWestern; // 동서양 영역 요구 권수

    @Column(name = "required_count_science", nullable = false)
    private Integer requiredCountScience; // 과학 영역 요구 권수

    @Column(name = "note")
    private String note; // 비고

    public ClassicCertCriterion(
        Integer admissionYear,
        Integer admissionYearShort,
        Integer totalRequiredCount,
        Integer requiredCountWestern,
        Integer requiredCountEastern,
        Integer requiredCountEasternAndWestern,
        Integer requiredCountScience,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.totalRequiredCount = totalRequiredCount;
        this.requiredCountWestern = requiredCountWestern;
        this.requiredCountEastern = requiredCountEastern;
        this.requiredCountEasternAndWestern = requiredCountEasternAndWestern;
        this.requiredCountScience = requiredCountScience;
        this.note = note;
    }
}
