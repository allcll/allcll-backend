package kr.allcll.backend.domain.graduation.credit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.allcll.backend.domain.graduation.balance.dto.BalanceAreaCoursesResponse;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoryResponse;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UncompletedCourseFilter {

    private final CourseReplacementResolver courseReplacementResolver;

    public List<GraduationCategoryResponse> filterUncompletedCourses(
        Integer admissionYear,
        List<GraduationCategoryResponse> categories,
        List<CompletedCourse> earnedCourses
    ) {
        Set<String> earnedCuriNos = buildEarnedCuriNos(admissionYear, earnedCourses);

        return categories.stream()
            .map(category -> filterCategory(category, earnedCuriNos))
            .toList();
    }

    private Set<String> buildEarnedCuriNos(Integer admissionYear, List<CompletedCourse> earnedCourses) {
        Set<String> earnedCuriNos = new HashSet<>();
        Set<String> earnedCuriNms = new HashSet<>();

        for (CompletedCourse completedCourse : earnedCourses) {
            String curiNo = completedCourse.getCuriNo();
            earnedCuriNos.add(curiNo);

            String curiNm = completedCourse.getCuriNm();
            String trimmedCuriNm = curiNm.trim();
            if (!trimmedCuriNm.isBlank()) {
                earnedCuriNms.add(trimmedCuriNm);
            }
        }

        earnedCuriNos.addAll(courseReplacementResolver.resolveCurrentCuriNos(admissionYear, earnedCuriNms));

        return earnedCuriNos;
    }

    private GraduationCategoryResponse filterCategory(GraduationCategoryResponse category, Set<String> earnedCuriNos) {
        List<RequiredCourseResponse> requiredCourses = filterCourses(category.requiredCourses(), earnedCuriNos);
        List<BalanceAreaCoursesResponse> balanceCourses = filterBalanceAreas(category, earnedCuriNos);

        return new GraduationCategoryResponse(
            category.majorScope(),
            category.categoryType(),
            category.isEnabled(),
            category.requiredCredits(),
            requiredCourses,
            category.requiredAreasCnt(),
            balanceCourses,
            category.excludedArea()
        );
    }

    private List<BalanceAreaCoursesResponse> filterBalanceAreas(
        GraduationCategoryResponse category,
        Set<String> earnedCuriNos
    ) {
        if (category.balanceAreaCourses() == null) {
            return null;
        }

        return category.balanceAreaCourses().stream()
            .map(balanceAreaCourse -> BalanceAreaCoursesResponse.of(
                balanceAreaCourse.balanceRequiredArea(),
                filterCourses(balanceAreaCourse.requiredCourses(), earnedCuriNos)
            ))
            .toList();
    }

    private List<RequiredCourseResponse> filterCourses(
        List<RequiredCourseResponse> requiredCourses,
        Set<String> earnedCuriNos
    ) {
        if (requiredCourses == null || requiredCourses.isEmpty()) {
            return List.of();
        }
        return requiredCourses.stream()
            .filter(requiredCourse -> !earnedCuriNos.contains(requiredCourse.curiNo()))
            .toList();
    }
}
