package kr.allcll.backend.domain.graduation.check.result.dto;

import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;

public record CertResult(
    String ruleType,
    Integer passedCount,
    Integer requiredPassCount,
    Boolean isSatisfied,
    Boolean isEnglishCertPassed,
    Boolean isCodingCertPassed,
    Boolean isClassicsCertPassed,
    Integer classicsTotalRequiredCount,
    Integer classicsTotalMyCount,
    Integer requiredCountWestern,
    Integer myCountWestern,
    Boolean isClassicsWesternCertPassed,
    Integer requiredCountEastern,
    Integer myCountEastern,
    Boolean isClassicsEasternCertPassed,
    Integer requiredCountEasternAndWestern,
    Integer myCountEasternAndWestern,
    Boolean isClassicsEasternAndWesternCertPassed,
    Integer requiredCountScience,
    Integer myCountScience,
    Boolean isClassicsScienceCertPassed
) {

    public static CertResult from(GraduationCheckCertResult certResult) {
        return new CertResult(
            certResult.getGraduationCertRuleType().name(),
            certResult.getPassedCount(),
            certResult.getRequiredPassCount(),
            certResult.getIsSatisfied(),
            certResult.getIsEnglishCertPassed(),
            certResult.getIsCodingCertPassed(),
            certResult.getIsClassicsCertPassed(),
            certResult.getClassicsTotalRequiredCount(),
            certResult.getClassicsTotalMyCount(),
            certResult.getRequiredCountWestern(),
            certResult.getMyCountWestern(),
            certResult.getIsClassicsWesternCertPassed(),
            certResult.getRequiredCountEastern(),
            certResult.getMyCountEastern(),
            certResult.getIsClassicsEasternCertPassed(),
            certResult.getRequiredCountEasternAndWestern(),
            certResult.getMyCountEasternAndWestern(),
            certResult.getIsClassicsEasternAndWesternCertPassed(),
            certResult.getRequiredCountScience(),
            certResult.getMyCountScience(),
            certResult.getIsClassicsScienceCertPassed()
        );
    }
}
