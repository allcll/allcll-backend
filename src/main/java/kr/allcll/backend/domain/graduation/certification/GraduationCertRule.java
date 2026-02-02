package kr.allcll.backend.domain.graduation.certification;

import jakarta.persistence.*;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "graduation_cert_rules")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraduationCertRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear; // 입학년도

    @Column(name = "admission_year_short", nullable = false)
    private Integer admissionYearShort;

    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_cert_rule_type", nullable = false)
    private GraduationCertRuleType graduationCertRuleType; // 인증 검사 방식

    @Column(name = "required_pass_count", nullable = false)
    private Integer requiredPassCount; // 최소 통과 개수

    @Column(name = "enable_english", nullable = false)
    private Boolean enableEnglish; // 영어인증 검사 포함 여부

    @Column(name = "enable_classic", nullable = false)
    private Boolean enableClassic; // 고전독서인증 검사 포함 여부

    @Column(name = "enable_coding", nullable = false)
    private Boolean enableCoding; // 코딩인증 검사 포함 여부

    @Column(name = "note")
    private String note; // 비고

    public GraduationCertRule(
        Integer admissionYear,
        Integer admissionYearShort,
        GraduationCertRuleType graduationCertRuleType,
        Integer requiredPassCount,
        Boolean enableEnglish,
        Boolean enableClassic,
        Boolean enableCoding,
        String note
    ) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.graduationCertRuleType = graduationCertRuleType;
        this.requiredPassCount = requiredPassCount;
        this.enableEnglish = enableEnglish;
        this.enableClassic = enableClassic;
        this.enableCoding = enableCoding;
        this.note = note;
    }
}
