package kr.allcll.backend.domain.graduation.credit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

        List<RequiredCourse> requiredCourses = selectedByCourseKey.values().stream()
            .filter(RequiredCourse::getRequired)
            .toList();
        Map<String, RequiredCourse> currentCoursesBySameCourseCode = findCurrentCourses(requiredCourses);

        return requiredCourses.stream()
            .collect(Collectors.groupingBy(
                RequiredCourse::getCategoryType,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    categoryCourses -> resolveDeprecatedCourses(categoryCourses, currentCoursesBySameCourseCode)
                )
            ));
    }

    /**
     * DEPRECATED 지정과목의 현행 과목을 동일과목 그룹 단위로 한 번에 조회한다.
     */
    private Map<String, RequiredCourse> findCurrentCourses(List<RequiredCourse> requiredCourses) {
        Set<String> deprecatedSameCourseCodes = requiredCourses.stream()
            .filter(requiredCourse -> isDeprecated(requiredCourse.getCuriNo()))
            .map(RequiredCourse::getSameCourseCode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (deprecatedSameCourseCodes.isEmpty()) {
            return Map.of();
        }

        Map<String, RequiredCourse> currentCoursesBySameCourseCode = new HashMap<>();
        requiredCourseRepository.findCurrentCoursesBySameCourseCodes(deprecatedSameCourseCodes, DEPRECATED)
            .forEach(currentCourse ->
                currentCoursesBySameCourseCode.putIfAbsent(currentCourse.getSameCourseCode(), currentCourse)
            );
        return currentCoursesBySameCourseCode;
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

    private List<RequiredCourseResponse> resolveDeprecatedCourses(
        List<RequiredCourse> requiredCourses,
        Map<String, RequiredCourse> currentCoursesBySameCourseCode
    ) {
        return requiredCourses.stream()
            .map(requiredCourse -> mapToCurrentCourse(requiredCourse, currentCoursesBySameCourseCode))
            .toList();
    }

    private boolean isWildCardRule(RequiredCourse requiredCourse, String departmentName) {
        return !isSpecificRule(requiredCourse, departmentName) && requiredCourse.getDeptNm().equals(WILD_CARD_DEPT_NM);
    }

    private boolean isSpecificRule(RequiredCourse requiredCourse, String departmentName) {
        return requiredCourse.getDeptNm().equals(departmentName);
    }

    private RequiredCourseResponse mapToCurrentCourse(
        RequiredCourse requiredCourse,
        Map<String, RequiredCourse> currentCoursesBySameCourseCode
    ) {
        if (isNotDeprecated(requiredCourse.getCuriNo())) {
            return RequiredCourseResponse.of(requiredCourse.getCuriNo(), requiredCourse.getCuriNm());
        }
        RequiredCourse currentCourse = currentCoursesBySameCourseCode.get(requiredCourse.getSameCourseCode());
        if (currentCourse == null) {
            log.error(
                "[졸업요건] DEPRECATED된 과목의 현재 과목 조회에 실패했습니다. sameCourseCode={}", requiredCourse.getSameCourseCode()
            );
            return RequiredCourseResponse.of(requiredCourse.getCuriNo(), requiredCourse.getCuriNm());
        }
        return RequiredCourseResponse.of(currentCourse.getCuriNo(), currentCourse.getCuriNm());
    }

    private boolean isNotDeprecated(String curiNo) {
        return !isDeprecated(curiNo);
    }

    private boolean isDeprecated(String curiNo) {
        return DEPRECATED.equals(curiNo);
    }
}
