package kr.allcll.backend.admin.graduation.dto;

import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.CompletedCoursesResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCheckResponse;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoriesResponse;

public record GraduationDetailResponse(
    GraduationUserResponse user,
    GraduationCheckResponse checkData,
    CompletedCoursesResponse courses,
    GraduationCategoriesResponse criteriaCategories,
    GraduationCertCriteriaResponse certificationCriteria
) {

}
