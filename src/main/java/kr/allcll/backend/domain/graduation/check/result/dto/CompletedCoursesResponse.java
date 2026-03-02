package kr.allcll.backend.domain.graduation.check.result.dto;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;

public record CompletedCoursesResponse(
    List<CompletedCourseResponse> value
) {

    public static CompletedCoursesResponse from(List<CompletedCourse> completedCourses) {
        return new CompletedCoursesResponse(completedCourses.stream()
            .map(CompletedCourseResponse::from)
            .toList()
        );
    }
}
