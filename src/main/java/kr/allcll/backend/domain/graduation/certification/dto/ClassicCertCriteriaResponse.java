package kr.allcll.backend.domain.graduation.certification.dto;

import kr.allcll.backend.domain.graduation.check.cert.ClassicsArea;

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

    public static ClassicCertCriteriaResponse fromEnum() {
        return new ClassicCertCriteriaResponse(
            ClassicsArea.getTotalRequiredCount(),
            ClassicsArea.WESTERN.getMaxRecognizedCount(),
            ClassicsArea.EASTERN.getMaxRecognizedCount(),
            ClassicsArea.EASTERN_AND_WESTERN.getMaxRecognizedCount(),
            ClassicsArea.SCIENCE.getMaxRecognizedCount()
        );
    }
}
