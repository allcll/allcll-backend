package kr.allcll.backend.domain.graduation.check.result.dto;

import java.time.LocalDateTime;
import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourses;

public record CompletedCoursesResponse(
    LocalDateTime createdAt,
    List<CompletedCourseResponse> courses
) {

    public static CompletedCoursesResponse from(CompletedCourses completedCourses) {
        return new CompletedCoursesResponse(
            completedCourses.getCourseCreatedDate(),
            completedCourses.getCourses().stream()
                .map(CompletedCourseResponse::from)
                .toList()
        );
    }
}
