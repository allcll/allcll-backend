package kr.allcll.backend.domain.graduation.certification.dto;

public record EnglishCertCriteriaResponse(
    String englishTargetType,
    Integer toeicMinScore,
    Integer toeflIbtMinScore,
    Integer tepsMinScore,
    Integer newTepsMinScore,
    String opicMinLevel,
    String toeicSpeakingMinLevel,
    Integer gtelpLevel,
    Integer gtelpMinScore,
    Integer gtelpSpeakingLevel,
    EnglishCertAltCourseResponse altCourse
) {

    public static EnglishCertCriteriaResponse of(
        String englishTargetType,
        Integer toeicMinScore,
        Integer toeflIbtMinScore,
        Integer tepsMinScore,
        Integer newTepsMinScore,
        String opicMinLevel,
        String toeicSpeakingMinLevel,
        Integer gtelpLevel,
        Integer gtelpMinScore,
        Integer gtelpSpeakingLevel,
        EnglishCertAltCourseResponse altCourse
    ) {
        return new EnglishCertCriteriaResponse(
            englishTargetType,
            toeicMinScore,
            toeflIbtMinScore,
            tepsMinScore,
            newTepsMinScore,
            opicMinLevel,
            toeicSpeakingMinLevel,
            gtelpLevel,
            gtelpMinScore,
            gtelpSpeakingLevel,
            altCourse
        );
    }
}
