package kr.allcll.backend.domain.graduation.certification.dto;

public record GraduationCertCriteriaResponse(
    GraduationCertCriteriaTargetResponse criteriaTarget,
    GraduationCertPolicyResponse certPolicy,
    EnglishCertCriteriaResponse englishCertCriteria,
    ClassicCertCriteriaResponse classicCertCriteria,
    CodingCertCriteriaResponse codingCertCriteria
) {

    public static GraduationCertCriteriaResponse of(
        GraduationCertCriteriaTargetResponse criteriaTarget,
        GraduationCertPolicyResponse certPolicy,
        EnglishCertCriteriaResponse englishCertCriteria,
        ClassicCertCriteriaResponse classicCertCriteria,
        CodingCertCriteriaResponse codingCertCriteria
    ) {
        return new GraduationCertCriteriaResponse(
            criteriaTarget,
            certPolicy,
            englishCertCriteria,
            classicCertCriteria,
            codingCertCriteria
        );
    }
}
