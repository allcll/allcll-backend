package kr.allcll.backend.domain.graduation.credit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import kr.allcll.backend.support.graduation.KeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequiredCourseResolver {

    private static final String DEPRECATED = "DEPRECATED";
    private static final String WILD_CARD_DEPT_NM = "ALL";
    private static final String WILD_CARD_DEPT_CD = "0";
    private final RequiredCourseRepository requiredCourseRepository;

    public Map<CategoryType, List<RequiredCourseResponse>> resolveRequiredCourses(
        Integer admissionYear,
        String deptCd
    ) {
        return resolveRequiredCourses(requiredCourseRepository.findByAdmissionYearAndDepts(
            admissionYear,
            List.of(WILD_CARD_DEPT_CD, deptCd)
        ));
    }

    private Map<CategoryType, List<RequiredCourseResponse>> resolveRequiredCourses(List<RequiredCourse> courses) {
        Map<String, RequiredCourse> selectedByCourseKey = new LinkedHashMap<>();
        for (RequiredCourse course : courses) {
            String courseKey = KeyUtils.generate(course.getCategoryType(), course.getCuriNm());
            if (WILD_CARD_DEPT_CD.equals(course.getDeptCd())) {
                selectedByCourseKey.putIfAbsent(courseKey, course);
                continue;
            }
            selectedByCourseKey.put(courseKey, course);
        }

        return selectedByCourseKey.values().stream()
            .filter(RequiredCourse::getRequired)
            .collect(Collectors.groupingBy(
                RequiredCourse::getCategoryType,
                Collectors.collectingAndThen(Collectors.toList(), this::resolveDeprecatedCourses)
            ));
    }

    public boolean findRequiredCourseInGroup(
        String departmentName,
        Integer admissionYear,
        CategoryType categoryType,
        String sameCourseCode
    ) {
        List<RequiredCourse> requiredCourseCandidatesWithWildCard =
            requiredCourseRepository.findRequiredCoursesBySameCourseCode(
                List.of(WILD_CARD_DEPT_NM, departmentName),
                admissionYear,
                categoryType,
                sameCourseCode
            );

        List<RequiredCourse> requiredCoursesWithStatus
            = getDepartmentRequiredCourses(requiredCourseCandidatesWithWildCard, departmentName);

        return requiredCoursesWithStatus.stream()
            .anyMatch(RequiredCourse::getRequired);
    }

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

    public List<RequiredCourseResponse> resolveDeprecatedCourses(List<RequiredCourse> requiredCourses) {
        return requiredCourses.stream()
            .map(this::mapToCurrentCourse)
            .toList();
    }

    private boolean isWildCardRule(RequiredCourse requiredCourse, String departmentName) {
        return !isSpecificRule(requiredCourse, departmentName) && requiredCourse.getDeptNm().equals(WILD_CARD_DEPT_NM);
    }

    private boolean isSpecificRule(RequiredCourse requiredCourse, String departmentName) {
        return requiredCourse.getDeptNm().equals(departmentName);
    }

    private RequiredCourseResponse mapToCurrentCourse(RequiredCourse requiredCourse) {
        if (isNotDeprecated(requiredCourse.getCuriNo())) {
            return RequiredCourseResponse.of(requiredCourse.getCuriNo(), requiredCourse.getCuriNm());
        }
        return requiredCourseRepository.findCurrentCourseBySameCourseCode(requiredCourse.getSameCourseCode(), DEPRECATED)
            .map(currentCourse -> RequiredCourseResponse.of(currentCourse.getCuriNo(), currentCourse.getCuriNm()))
            .orElseGet(() -> {
                log.error(
                    "[졸업요건] DEPRECATED된 과목의 현재 과목 조회에 실패했습니다. sameCourseCode={}", requiredCourse.getSameCourseCode()
                );
                return RequiredCourseResponse.of(requiredCourse.getCuriNo(), requiredCourse.getCuriNm());
            });
    }

    private boolean isNotDeprecated(String curiNo) {
        return !isDeprecated(curiNo);
    }

    private boolean isDeprecated(String curiNo) {
        return DEPRECATED.equals(curiNo);
    }
}
