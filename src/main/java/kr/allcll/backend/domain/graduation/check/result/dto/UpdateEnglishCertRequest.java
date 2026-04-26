package kr.allcll.backend.domain.graduation.check.result.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateEnglishCertRequest(
    @NotNull
    Boolean isPassed
) {

}
