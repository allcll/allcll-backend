package kr.allcll.backend.admin.graduation.dto;

import kr.allcll.backend.domain.graduation.certification.dto.ClassicCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.certification.dto.CodingCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.certification.dto.EnglishCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaResponse;

public record GraduationCertificationCriteriaResponse(
    EnglishCertCriteriaResponse englishCertCriteria,
    ClassicCertCriteriaResponse classicCertCriteria,
    CodingCertCriteriaResponse codingCertCriteria
) {

    public static GraduationCertificationCriteriaResponse from(GraduationCertCriteriaResponse response) {
        return new GraduationCertificationCriteriaResponse(
            response.englishCertCriteria(),
            response.classicCertCriteria(),
            response.codingCertCriteria()
        );
    }
}
