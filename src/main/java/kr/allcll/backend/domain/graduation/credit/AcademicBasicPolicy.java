package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AcademicBasicPolicy {

    private final RequiredCourseResolver requiredCourseResolver;
    private final CourseEquivalenceRepository courseEquivalenceRepository;

    public List<CompletedCourse> filterRecentMajorAcademicBasic(
        List<CompletedCourse> courses,
        CreditCriterion criterion
    ) {
        List<CompletedCourse> academicBasicCourses = courses.stream()
            .filter(this::isAcademicBasic)
            .toList();
        if (academicBasicCourses.isEmpty()) {
            return courses;
        }

        List<String> requiredCourseNames = requiredCourseResolver.findRequiredCourseNames(
            criterion.getDeptNm(),
            criterion.getAdmissionYear(),
            CategoryType.ACADEMIC_BASIC
        );

        List<CompletedCourse> unmatchedAcademicBasicCourses = academicBasicCourses.stream()
            .filter(course -> !requiredCourseNames.contains(course.getCuriNm()))
            .toList();

        Set<String> equivalenceCuriNos = findEquivalenceCuriNos(
            unmatchedAcademicBasicCourses,
            criterion
        );

        return courses.stream()
            .filter(course -> isRecentMajorAcademicBasic(
                course,
                requiredCourseNames,
                equivalenceCuriNos
            ))
            .toList();
    }

    private boolean isRecentMajorAcademicBasic(
        CompletedCourse course,
        List<String> requiredCourseNames,
        Set<String> equivalenceCuriNos
    ) {
        if (!isAcademicBasic(course)) {
            return true;
        }

        return requiredCourseNames.contains(course.getCuriNm())
            || equivalenceCuriNos.contains(course.getCuriNo());
    }

    private Set<String> findEquivalenceCuriNos(
        List<CompletedCourse> unmatchedAcademicBasicCourses,
        CreditCriterion criterion
    ) {
        Set<String> curiNos = unmatchedAcademicBasicCourses.stream()
            .map(CompletedCourse::getCuriNo)
            .collect(Collectors.toSet());

        Map<String, Set<String>> sameCourseCodesByCuriNo = findSameCourseCodesByCuriNo(curiNos);
        if (sameCourseCodesByCuriNo.isEmpty()) {
            return Set.of();
        }

        Set<String> requiredSameCourseCodes = findRequiredSameCourseCodes(
            sameCourseCodesByCuriNo,
            criterion
        );

        return sameCourseCodesByCuriNo.entrySet().stream()
            .filter(entry -> hasRequiredSameCourseCode(entry.getValue(), requiredSameCourseCodes))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    private Map<String, Set<String>> findSameCourseCodesByCuriNo(Set<String> curiNos) {
        if (curiNos.isEmpty()) {
            return Map.of();
        }

        return courseEquivalenceRepository.findAllByCuriNoIn(curiNos).stream()
            .collect(Collectors.groupingBy(
                CourseEquivalence::getCuriNo,
                Collectors.mapping(CourseEquivalence::getSameCourseCode, Collectors.toSet())
            ));
    }

    private Set<String> findRequiredSameCourseCodes(
        Map<String, Set<String>> sameCourseCodesByCuriNo,
        CreditCriterion criterion
    ) {
        Set<String> sameCourseCodes = sameCourseCodesByCuriNo.values().stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        return requiredCourseResolver.findRequiredCourseInGroups(
            criterion.getDeptNm(),
            criterion.getAdmissionYear(),
            CategoryType.ACADEMIC_BASIC,
            sameCourseCodes
        );
    }

    private boolean hasRequiredSameCourseCode(
        Set<String> sameCourseCodes,
        Set<String> requiredSameCourseCodes
    ) {
        return sameCourseCodes.stream()
            .anyMatch(requiredSameCourseCodes::contains);
    }

    private boolean isAcademicBasic(CompletedCourse course) {
        return CategoryType.ACADEMIC_BASIC.equals(course.getCategoryType());
    }
}
