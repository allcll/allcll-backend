package kr.allcll.backend.domain.graduation.check.result.dto;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.credit.CategoryType;

public record RequiredCourseRecommendation(
    MajorScope majorScope,
    CategoryType categoryType,
    List<MissingCourse> missingCourses
) {

}
