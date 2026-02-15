package kr.allcll.backend.domain.graduation.certification.dto;

public record GraduationCertPolicyResponse(
    String graduationCertRuleType,
    Integer requiredPassCount,
    Boolean enableEnglish,
    Boolean enableClassic,
    Boolean enableCoding
) {

    public static GraduationCertPolicyResponse of(
        String graduationCertRuleType,
        Integer requiredPassCount,
        Boolean enableEnglish,
        Boolean enableClassic,
        Boolean enableCoding
    ) {
        return new GraduationCertPolicyResponse(
            graduationCertRuleType,
            requiredPassCount,
            enableEnglish,
            enableClassic,
            enableCoding
        );
    }
}
