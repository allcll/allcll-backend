package kr.allcll.backend.domain.graduation.check.cert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import kr.allcll.backend.domain.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "graduation_check_cert_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraduationCheckCertResult {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_cert_rule_type", nullable = false)
    private GraduationCertRuleType graduationCertRuleType;  // 인증 정책 타입

    @Column(name = "passed_count", nullable = false)
    private Integer passedCount;  // 통과한 항목 개수

    @Column(name = "required_pass_count", nullable = false)
    private Integer requiredPassCount;  // 필요한 항목 개수

    @Column(name = "is_satisfied", nullable = false)
    private Boolean isSatisfied;  // 최종 졸업인증제도 통과 여부

    @Column(name = "is_english_cert_passed", nullable = false)
    private Boolean isEnglishCertPassed;  // 영어 인증 통과 여부

    @Column(name = "is_coding_cert_passed", nullable = false)
    private Boolean isCodingCertPassed;  // 코딩 인증 통과 여부

    @Column(name = "is_classics_cert_passed", nullable = false)
    private Boolean isClassicsCertPassed;  // 고전독서 인증 통과 여부

    @Column(name = "classics_total_required_count", nullable = false)
    private Integer classicsTotalRequiredCount;  // 전체 요구 권수

    @Column(name = "classics_total_my_count", nullable = false)
    private Integer classicsTotalMyCount;  // 내 총 인증 권수

    @Column(name = "required_count_western", nullable = false)
    private Integer requiredCountWestern;  // 서양의 역사와 사상 요구 권수

    @Column(name = "my_count_western", nullable = false)
    private Integer myCountWestern;  // 서양의 역사와 사상 내 인증 권수

    @Column(name = "is_classics_western_cert_passed", nullable = false)
    private Boolean isClassicsWesternCertPassed;  // 서양의 역사와 사상 통과 여부 (선택)

    @Column(name = "required_count_eastern", nullable = false)
    private Integer requiredCountEastern;  // 동양의 역사와 사상 요구 권수

    @Column(name = "my_count_eastern", nullable = false)
    private Integer myCountEastern;  // 동양의 역사와 사상 내 인증 권수

    @Column(name = "is_classics_eastern_cert_passed", nullable = false)
    private Boolean isClassicsEasternCertPassed;  // 동양의 역사와 사상 통과 여부 (선택)

    @Column(name = "required_count_eastern_and_western", nullable = false)
    private Integer requiredCountEasternAndWestern;  // 동서양의 문학 요구 권수

    @Column(name = "my_count_eastern_and_western", nullable = false)
    private Integer myCountEasternAndWestern;  // 동서양의 문학 내 인증 권수

    @Column(name = "is_classics_eastern_and_western_cert_passed", nullable = false)
    private Boolean isClassicsEasternAndWesternCertPassed;  // 동서양의 문학 통과 여부 (선택)

    @Column(name = "required_count_science", nullable = false)
    private Integer requiredCountScience;  // 과학 사상 요구 권수

    @Column(name = "my_count_science", nullable = false)
    private Integer myCountScience;  // 과학 사상 내 인증 권수

    @Column(name = "is_classics_science_cert_passed", nullable = false)
    private Boolean isClassicsScienceCertPassed;  // 과학 사상 통과 여부 (선택)

    public GraduationCheckCertResult(User user, GraduationCertRuleType graduationCertRuleType, Integer passedCount,
        Integer requiredPassCount, Boolean isSatisfied, Boolean isEnglishCertPassed, Boolean isCodingCertPassed,
        Boolean isClassicsCertPassed,
        Integer classicsTotalRequiredCount, Integer classicsTotalMyCount, Integer requiredCountWestern,
        Integer myCountWestern, Boolean isClassicsWesternCertPassed, Integer requiredCountEastern,
        Integer myCountEastern, Boolean isClassicsEasternCertPassed, Integer requiredCountEasternAndWestern,
        Integer myCountEasternAndWestern, Boolean isClassicsEasternAndWesternCertPassed,
        Integer requiredCountScience,
        Integer myCountScience, Boolean isClassicsScienceCertPassed) {
        this.user = user;
        this.userId = user.getId();
        this.graduationCertRuleType = graduationCertRuleType;
        this.passedCount = passedCount;
        this.requiredPassCount = requiredPassCount;
        this.isSatisfied = isSatisfied;
        this.isEnglishCertPassed = isEnglishCertPassed;
        this.isCodingCertPassed = isCodingCertPassed;
        this.isClassicsCertPassed = isClassicsCertPassed;
        this.classicsTotalRequiredCount = classicsTotalRequiredCount;
        this.classicsTotalMyCount = classicsTotalMyCount;
        this.requiredCountWestern = requiredCountWestern;
        this.myCountWestern = myCountWestern;
        this.isClassicsWesternCertPassed = isClassicsWesternCertPassed;
        this.requiredCountEastern = requiredCountEastern;
        this.myCountEastern = myCountEastern;
        this.isClassicsEasternCertPassed = isClassicsEasternCertPassed;
        this.requiredCountEasternAndWestern = requiredCountEasternAndWestern;
        this.myCountEasternAndWestern = myCountEasternAndWestern;
        this.isClassicsEasternAndWesternCertPassed = isClassicsEasternAndWesternCertPassed;
        this.requiredCountScience = requiredCountScience;
        this.myCountScience = myCountScience;
        this.isClassicsScienceCertPassed = isClassicsScienceCertPassed;
    }
}
