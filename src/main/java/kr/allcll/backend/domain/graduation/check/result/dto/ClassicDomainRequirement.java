package kr.allcll.backend.domain.graduation.check.result.dto;

public record ClassicDomainRequirement(
    String domainType,
    Integer requiredCount,
    Integer myCount,
    Boolean satisfied
) {

}
