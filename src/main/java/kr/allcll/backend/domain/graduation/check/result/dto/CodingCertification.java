package kr.allcll.backend.domain.graduation.check.result.dto;

import kr.allcll.backend.domain.graduation.certification.CodingTargetType;

public record CodingCertification(
    Boolean isRequired,
    Boolean isPassed,
    CodingTargetType targetType
) {

}
