package kr.allcll.backend.domain.graduation.check.result.dto;

import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.credit.CategoryType;

public record GraduationCategory(
    MajorScope majorScope,
    CategoryType categoryType,
    Double earnedCredits,
    Integer requiredCredits,
    Double remainingCredits,
    Boolean satisfied
) {

}