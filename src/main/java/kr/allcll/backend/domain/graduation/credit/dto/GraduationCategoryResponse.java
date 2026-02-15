package kr.allcll.backend.domain.graduation.credit.dto;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
import kr.allcll.backend.domain.graduation.balance.dto.BalanceAreaCoursesResponse;
import kr.allcll.backend.domain.graduation.credit.CategoryType;

public record GraduationCategoryResponse(
    MajorScope majorScope,
    CategoryType categoryType,
    boolean isEnabled,
    Integer requiredCredits,
    List<RequiredCourseResponse> requiredCourses,

    Integer requiredAreasCnt,
    List<BalanceAreaCoursesResponse> balanceAreaCourses,
    BalanceRequiredArea excludedArea
) {

    public static GraduationCategoryResponse of(
        MajorScope majorScope,
        CategoryType categoryType,
        boolean isEnabled,
        Integer requiredCredits,
        List<RequiredCourseResponse> requiredCourses
    ) {
        return new GraduationCategoryResponse(
            majorScope,
            categoryType,
            isEnabled,
            requiredCredits,
            requiredCourses,
            null,
            null,
            null
        );
    }

    public static GraduationCategoryResponse balanceRequiredOf(
        CategoryType categoryType,
        boolean isEnabled,
        Integer requiredCredits,
        Integer requiredAreasCnt,
        List<BalanceAreaCoursesResponse> balanceAreaCourses,
        BalanceRequiredArea excludedArea
    ) {
        return new GraduationCategoryResponse(
            MajorScope.PRIMARY,
            categoryType,
            isEnabled,
            requiredCredits,
            List.of(),
            requiredAreasCnt,
            balanceAreaCourses,
            excludedArea
        );
    }
}
