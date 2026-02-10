package kr.allcll.backend.domain.graduation.credit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequiredCourseResolver {

    private static final String DEPRECATED = "DEPRECATED";

    private final CourseReplacementRepository courseReplacementRepository;

    public List<RequiredCourseResponse> replaceDeprecatedSubject(Integer admissionYear,
        List<RequiredCourse> requiredCourses) {
        Map<String, CourseReplacement> replacementByLegacyName = loadReplacementMap(admissionYear, requiredCourses);
        return requiredCourses.stream()
            .map(requiredCourse -> toResponse(requiredCourse, replacementByLegacyName))
            .toList();
    }

    private Map<String, CourseReplacement> loadReplacementMap(Integer admissionYear,
        List<RequiredCourse> requiredCourses) {
        List<String> replacementByLegacyName = requiredCourses.stream()
            .filter(this::isDeprecated)
            .map(RequiredCourse::getCuriNm)
            .distinct()
            .toList();

        if (replacementByLegacyName.isEmpty()) {
            return new HashMap<>();
        }

        return courseReplacementRepository
            .findByAdmissionYearAndLegacyCuriNmIn(admissionYear, replacementByLegacyName)
            .stream()
            .collect(Collectors.toMap(
                CourseReplacement::getLegacyCuriNm,
                courseReplacement -> courseReplacement
            ));
    }

    private RequiredCourseResponse toResponse(RequiredCourse requiredCourse, Map<String, CourseReplacement> replacementByLegacyName) {
        if (isDeprecated(requiredCourse)) {
            CourseReplacement courseReplacement = replacementByLegacyName.get(requiredCourse.getCuriNm());
            return RequiredCourseResponse.of(courseReplacement.getCurrentCuriNo(), courseReplacement.getCurrentCuriNm());
        }
        return RequiredCourseResponse.of(requiredCourse.getCuriNo(), requiredCourse.getCuriNm());
    }

    private boolean isDeprecated(RequiredCourse course) {
        return DEPRECATED.equals(course.getCuriNo());
    }
}
