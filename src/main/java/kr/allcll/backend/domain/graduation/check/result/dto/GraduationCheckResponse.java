package kr.allcll.backend.domain.graduation.check.result.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GraduationCheckResponse(
    Long checkId,
    LocalDateTime updatedAt,
    Boolean isGraduatable,
    GraduationSummary summary,
    List<GraduationCategory> categories,
    //GraduationRecommendations recommendations,
    GraduationCertifications certifications
) {

}
