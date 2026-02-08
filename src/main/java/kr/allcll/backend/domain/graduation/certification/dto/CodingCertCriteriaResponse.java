package kr.allcll.backend.domain.graduation.certification.dto;

public record CodingCertCriteriaResponse(
    String codingTargetType,
    Integer toscMinLevel,
    CodingCertAltCourseResponse altCourse
) {

    public static CodingCertCriteriaResponse of(
        String codingTargetType,
        Integer toscMinLevel,
        CodingCertAltCourseResponse altCourse
    ) {
        return new CodingCertCriteriaResponse(codingTargetType, toscMinLevel, altCourse);
    }
}

