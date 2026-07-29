package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import java.util.Map;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneralElectivePolicy {

    private final CreditCriterionRepository creditCriterionRepository;
    private final RequiredCourseResolver requiredCourseResolver;
    private final UncompletedCourseFilter uncompletedCourseFilter;

    public boolean areRequiredCoursesSatisfied(User user, List<CompletedCourse> earnedCourses) {
        if (!hasEnabledCriterion(user)) {
            return true;
        }

        Map<CategoryType, List<RequiredCourseResponse>> requiredCoursesByCategory = requiredCourseResolver
            .resolveRequiredCourses(user.getAdmissionYear(), user.getDeptCd());
        List<RequiredCourseResponse> requiredCourses = requiredCoursesByCategory
            .getOrDefault(CategoryType.GENERAL_ELECTIVE, List.of());
        return uncompletedCourseFilter.filterUncompletedRequiredCourses(requiredCourses, earnedCourses).isEmpty();
    }

    private boolean hasEnabledCriterion(User user) {
        return creditCriterionRepository
            .findByAdmissionYearAndMajorTypeAndDeptCd(user.getAdmissionYear(), MajorType.ALL, user.getDeptCd())
            .stream()
            .anyMatch(criterion -> criterion.getCategoryType() == CategoryType.GENERAL_ELECTIVE
                && criterion.getEnabled());
    }
}
