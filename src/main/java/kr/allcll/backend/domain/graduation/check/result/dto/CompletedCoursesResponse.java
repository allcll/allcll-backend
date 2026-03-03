package kr.allcll.backend.domain.graduation.check.result.dto;

import java.time.LocalDateTime;
import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;

public record CompletedCoursesResponse(
    LocalDateTime createdAt,
    List<CompletedCourseResponse> courses
) {

    public static CompletedCoursesResponse from(LocalDateTime createdAt, List<CompletedCourse> completedCourses) {
        return new CompletedCoursesResponse(
            createdAt,
            completedCourses.stream()
                .map(CompletedCourseResponse::from)
                .toList()
        );
    }
}
