package kr.allcll.backend.domain.graduation.check.result.dto;

import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.credit.CategoryType;

public record CompletedCourseResponse(
    Long id,
    String curiNo,
    String curiNm,
    CategoryType categoryType,
    String selectedArea,
    Double credits,
    MajorScope majorScope,
    boolean isEarned
) {

    public static CompletedCourseResponse from(CompletedCourse completedCourse) {
        return new CompletedCourseResponse(
            completedCourse.getId(),
            completedCourse.getCuriNo(),
            completedCourse.getCuriNm(),
            completedCourse.getCategoryType(),
            completedCourse.getSelectedArea(),
            completedCourse.getCredits(),
            completedCourse.getMajorScope(),
            completedCourse.isEarned()
        );
    }
}
