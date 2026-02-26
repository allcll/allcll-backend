package kr.allcll.backend.domain.graduation.check.result.dto;

import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;

public record EnglishCertification(
    Boolean isRequired,
    Boolean isPassed,
    EnglishTargetType targetType
) {

}
