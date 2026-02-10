package kr.allcll.backend.domain.graduation.check.result.dto;

public record CertificationPolicy(
    String ruleType,
    Integer requiredPassCount
) {

}
