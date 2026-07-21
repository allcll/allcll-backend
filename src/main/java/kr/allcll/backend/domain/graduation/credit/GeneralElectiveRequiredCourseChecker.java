package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoryResponse;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneralElectiveRequiredCourseChecker {

    private final NonMajorCategoryResolver nonMajorCategoryResolver;
    private final UncompletedCourseFilter uncompletedCourseFilter;

    public boolean isSatisfied(User user, List<CompletedCourse> earnedCourses) {
        List<GraduationCategoryResponse> generalElectiveCategories = nonMajorCategoryResolver
            .resolve(user.getAdmissionYear(), user.getDeptCd())
            .stream()
            .filter(category -> category.categoryType() == CategoryType.GENERAL_ELECTIVE)
            .toList();

        return uncompletedCourseFilter.filterUncompletedCourses(generalElectiveCategories, earnedCourses)
            .stream()
            .allMatch(category -> category.requiredCourses().isEmpty());
    }
}
