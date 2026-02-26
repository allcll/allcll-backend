package kr.allcll.backend.domain.graduation.certification.dto;

public record ClassicCertCriteriaResponse(
    Integer totalRequiredCount,
    Integer requiredCountWestern,
    Integer requiredCountEastern,
    Integer requiredCountEasternAndWestern,
    Integer requiredCountScience
) {

    public static ClassicCertCriteriaResponse of(
        Integer totalRequiredCount,
        Integer requiredCountWestern,
        Integer requiredCountEastern,
        Integer requiredCountEasternAndWestern,
        Integer requiredCountScience
    ) {
        return new ClassicCertCriteriaResponse(
            totalRequiredCount,
            requiredCountWestern,
            requiredCountEastern,
            requiredCountEasternAndWestern,
            requiredCountScience
        );
    }
}
