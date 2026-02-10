package kr.allcll.backend.domain.graduation.check.result.dto;

import java.util.List;

public record GraduationRecommendations(
    List<RequiredCourseRecommendation> requiredCourses
) {

}
