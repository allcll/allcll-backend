package kr.allcll.backend.domain.graduation.check.cert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "graduation_check_cert_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraduationCheckCertResult {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;  // 사용자 id

    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_cert_rule_type", nullable = false)
    private GraduationCertRuleType graduationCertRuleType;  // 인증 정책 타입

    @Column(name = "passed_count", nullable = false)
    private Integer passedCount;  // 통과한 항목 개수

    @Column(name = "required_pass_count", nullable = false)
    private Integer requiredPassCount;  // 필요한 항목 개수

    @Column(name = "is_satisfied", nullable = false)
    private Boolean isSatisfied;  // 최종 졸업인증제도 통과 여부

    @Column(name = "english_passed", nullable = false)
    private Boolean englishPassed;  // 영어 인증 통과 여부

    @Column(name = "coding_passed", nullable = false)
    private Boolean codingPassed;  // 코딩 인증 통과 여부

    @Column(name = "classics_passed", nullable = false)
    private Boolean classicsPassed;  // 고전독서 인증 통과 여부

    @Column(name = "classics_total_required_count", nullable = false)
    private Integer classicsTotalRequiredCount;  // 전체 요구 권수

    @Column(name = "classics_total_my_count", nullable = false)
    private Integer classicsTotalMyCount;  // 내 총 인증 권수

    @Column(name = "classics_domain1_required_count", nullable = false)
    private Integer classicsDomain1RequiredCount;  // 서양의 역사와 사상 요구 권수

    @Column(name = "classics_domain1_my_count", nullable = false)
    private Integer classicsDomain1MyCount;  // 서양의 역사와 사상 내 인증 권수

    @Column(name = "classics_domain1_satisfied", nullable = false)
    private Boolean classicsDomain1Satisfied;  // 서양의 역사와 사상 통과 여부 (선택)

    @Column(name = "classics_domain2_required_count", nullable = false)
    private Integer classicsDomain2RequiredCount;  // 동양의 역사와 사상 요구 권수

    @Column(name = "classics_domain2_my_count", nullable = false)
    private Integer classicsDomain2MyCount;  // 동양의 역사와 사상 내 인증 권수

    @Column(name = "classics_domain2_satisfied", nullable = false)
    private Boolean classicsDomain2Satisfied;  // 동양의 역사와 사상 통과 여부 (선택)

    @Column(name = "classics_domain3_required_count", nullable = false)
    private Integer classicsDomain3RequiredCount;  // 동서양의 문학 요구 권수

    @Column(name = "classics_domain3_my_count", nullable = false)
    private Integer classicsDomain3MyCount;  // 동서양의 문학 내 인증 권수

    @Column(name = "classics_domain3_satisfied", nullable = false)
    private Boolean classicsDomain3Satisfied;  // 동서양의 문학 통과 여부 (선택)

    @Column(name = "classics_domain4_required_count", nullable = false)
    private Integer classicsDomain4RequiredCount;  // 과학 사상 요구 권수

    @Column(name = "classics_domain4_my_count", nullable = false)
    private Integer classicsDomain4MyCount;  // 과학 사상 내 인증 권수

    @Column(name = "classics_domain4_satisfied", nullable = false)
    private Boolean classicsDomain4Satisfied;  // 과학 사상 통과 여부 (선택)

    public GraduationCheckCertResult(
        Long userId, GraduationCertRuleType graduationCertRuleType,
        Integer passedCount, Integer requiredPassCount, Boolean isSatisfied, Boolean englishPassed,
        Boolean codingPassed,
        Boolean classicsPassed, Integer classicsTotalRequiredCount, Integer classicsTotalMyCount,
        Integer classicsDomain1RequiredCount, Integer classicsDomain1MyCount, Boolean classicsDomain1Satisfied,
        Integer classicsDomain2RequiredCount, Integer classicsDomain2MyCount, Boolean classicsDomain2Satisfied,
        Integer classicsDomain3RequiredCount, Integer classicsDomain3MyCount, Boolean classicsDomain3Satisfied,
        Integer classicsDomain4RequiredCount, Integer classicsDomain4MyCount, Boolean classicsDomain4Satisfied) {
        this.userId = userId;
        this.graduationCertRuleType = graduationCertRuleType;
        this.passedCount = passedCount;
        this.requiredPassCount = requiredPassCount;
        this.isSatisfied = isSatisfied;
        this.englishPassed = englishPassed;
        this.codingPassed = codingPassed;
        this.classicsPassed = classicsPassed;
        this.classicsTotalRequiredCount = classicsTotalRequiredCount;
        this.classicsTotalMyCount = classicsTotalMyCount;
        this.classicsDomain1RequiredCount = classicsDomain1RequiredCount;
        this.classicsDomain1MyCount = classicsDomain1MyCount;
        this.classicsDomain1Satisfied = classicsDomain1Satisfied;
        this.classicsDomain2RequiredCount = classicsDomain2RequiredCount;
        this.classicsDomain2MyCount = classicsDomain2MyCount;
        this.classicsDomain2Satisfied = classicsDomain2Satisfied;
        this.classicsDomain3RequiredCount = classicsDomain3RequiredCount;
        this.classicsDomain3MyCount = classicsDomain3MyCount;
        this.classicsDomain3Satisfied = classicsDomain3Satisfied;
        this.classicsDomain4RequiredCount = classicsDomain4RequiredCount;
        this.classicsDomain4MyCount = classicsDomain4MyCount;
        this.classicsDomain4Satisfied = classicsDomain4Satisfied;
    }
}
