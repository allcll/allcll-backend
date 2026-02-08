package kr.allcll.backend.domain.graduation.certification.dto;

public record GraduationCertCriteriaTargetResponse(
    String englishTargetType,
    String codingTargetType
) {

    public static GraduationCertCriteriaTargetResponse of(
        String englishTargetType,
        String codingTargetType
    ) {
        return new GraduationCertCriteriaTargetResponse(
            englishTargetType,
            codingTargetType
        );
    }
}
