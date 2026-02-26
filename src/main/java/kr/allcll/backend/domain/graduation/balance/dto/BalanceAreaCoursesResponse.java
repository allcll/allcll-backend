package kr.allcll.backend.domain.graduation.balance.dto;

import java.util.List;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;

public record BalanceAreaCoursesResponse(
    BalanceRequiredArea balanceRequiredArea,
    List<RequiredCourseResponse> requiredCourses
) {
    public static BalanceAreaCoursesResponse of(
        BalanceRequiredArea balanceRequiredArea,
        List<RequiredCourseResponse> requiredCourses
    ) {
        return new BalanceAreaCoursesResponse(balanceRequiredArea, requiredCourses);
    }
}
