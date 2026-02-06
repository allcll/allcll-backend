package kr.allcll.backend.domain.graduation.certification;

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

    public GraduationCertRule(Integer admissionYear, Integer admissionYearShort,
        GraduationCertRuleType graduationCertRuleType) {
        this.admissionYear = admissionYear;
        this.admissionYearShort = admissionYearShort;
        this.graduationCertRuleType = graduationCertRuleType;
    }
}
