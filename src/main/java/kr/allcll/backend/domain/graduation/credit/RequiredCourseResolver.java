package kr.allcll.backend.domain.graduation.credit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequiredCourseResolver {

    private static final String DEPRECATED = "DEPRECATED";

    private final CourseReplacementRepository courseReplacementRepository;

    public List<RequiredCourseResponse> replaceDeprecatedSubject(Integer admissionYear,
        List<RequiredCourse> requiredCourses) {
        Map<String, CourseReplacement> replacementByLegacyName = loadReplacementMap(admissionYear, requiredCourses);
        return requiredCourses.stream()
            .map(requiredCourse -> toResponse(admissionYear, requiredCourse, replacementByLegacyName))
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

    private RequiredCourseResponse toResponse(
        Integer admissionYear,
        RequiredCourse requiredCourse,
        Map<String, CourseReplacement> replacementByLegacyName
    ) {
        if (!isDeprecated(requiredCourse)) {
            return RequiredCourseResponse.of(requiredCourse.getCuriNo(), requiredCourse.getCuriNm());
        }
        CourseReplacement courseReplacement = replacementByLegacyName.get(requiredCourse.getCuriNm());
        if (courseReplacement == null) {
            log.error(
                "[졸업요건] 대체 과목 매핑에 실패했습니다. admissionYear={}, legacyCuriNm={}",
                admissionYear,
                requiredCourse.getCuriNm()
            );
            return RequiredCourseResponse.of(requiredCourse.getCuriNo(), requiredCourse.getCuriNm());
        }
        return RequiredCourseResponse.of(courseReplacement.getCurrentCuriNo(), courseReplacement.getCurrentCuriNm());
    }

    private boolean isDeprecated(RequiredCourse course) {
        return DEPRECATED.equals(course.getCuriNo());
    }
}
