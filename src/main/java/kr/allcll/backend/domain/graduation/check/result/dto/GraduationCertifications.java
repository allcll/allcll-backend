package kr.allcll.backend.domain.graduation.check.result.dto;

public record GraduationCertifications(
    CertificationPolicy policy,
    Integer passedCount,
    Integer requiredPassCount,
    Boolean isSatisfied,
    EnglishCertification english,
    CodingCertification coding,
    ClassicCertification classic
) {

}
