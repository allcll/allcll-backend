package kr.allcll.backend.domain.graduation.credit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequiredCourseResolver {

    private static final String WILD_CARD_DEPT_NM = "ALL";
    private final RequiredCourseRepository requiredCourseRepository;

    public List<String> findRequiredCourseNames(
        String departmentName,
        Integer admissionYear,
        CategoryType categoryType
    ) {
        List<RequiredCourse> requiredCourseCandidatesWithWildCard = requiredCourseRepository.findRequiredCourses(
            List.of(WILD_CARD_DEPT_NM, departmentName),
            admissionYear,
            categoryType
        );

        List<RequiredCourse> requiredCoursesWithStatus
            = getDepartmentRequiredCourses(requiredCourseCandidatesWithWildCard, departmentName);

        return requiredCoursesWithStatus.stream()
            .filter(RequiredCourse::getRequired)
            .map(RequiredCourse::getCuriNm)
            .toList();
    }

    private List<RequiredCourse> getDepartmentRequiredCourses(
        List<RequiredCourse> candidateRequiredCourses,
        String departmentName
    ) {
        Map<String, RequiredCourse> requiredCourseCandidates = new HashMap<>();
        for (RequiredCourse candidateRequiredCourse : candidateRequiredCourses) {
            String courseName = candidateRequiredCourse.getCuriNm();

            if (isSpecificRule(candidateRequiredCourse, departmentName)) {
                requiredCourseCandidates.put(courseName, candidateRequiredCourse);
                continue;
            }

            if (isWildCardRule(candidateRequiredCourse, departmentName)) {
                requiredCourseCandidates.putIfAbsent(courseName, candidateRequiredCourse);
            }
        }

        return requiredCourseCandidates.values().stream().toList();
    }

    private boolean isWildCardRule(RequiredCourse requiredCourse, String departmentName) {
        return !isSpecificRule(requiredCourse, departmentName) && requiredCourse.getDeptNm().equals(WILD_CARD_DEPT_NM);
    }

    private boolean isSpecificRule(RequiredCourse requiredCourse, String departmentName) {
        return requiredCourse.getDeptNm().equals(departmentName);
    }
}
