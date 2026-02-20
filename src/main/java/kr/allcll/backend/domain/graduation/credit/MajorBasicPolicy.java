package kr.allcll.backend.domain.graduation.credit;

import java.util.Objects;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MajorBasicPolicy {

    private static final int MAJOR_BASIC_INTRODUCED_ADMISSION_YEAR = 2024;

    public boolean matchesCriterionCategory(
        int admissionYear,
        CompletedCourseDto course,
        CreditCriterion criterion
    ) {
        CategoryType criterionCategoryType = criterion.getCategoryType();
        CategoryType courseCategoryType = course.categoryType();

        if (Objects.equals(courseCategoryType, criterionCategoryType)) {
            return true;
        }

        return shouldCountMajorBasicAsAcademicBasic(admissionYear, courseCategoryType, criterionCategoryType);
    }

    public CompletedCourseDto normalizeForAcademicBasicIfNeeded(
        int admissionYear,
        CompletedCourseDto course,
        CreditCriterion criterion
    ) {
        CategoryType criterionCategoryType = criterion.getCategoryType();
        CategoryType courseCategoryType = course.categoryType();

        if (shouldCountMajorBasicAsAcademicBasic(admissionYear, courseCategoryType, criterionCategoryType)) {
            return new CompletedCourseDto(
                course.curiNo(),
                course.curiNm(),
                CategoryType.ACADEMIC_BASIC,
                course.selectedArea(),
                course.credits(),
                course.grade(),
                course.majorScope()
            );
        }

        return course;
    }

    private boolean shouldCountMajorBasicAsAcademicBasic(
        int admissionYear,
        CategoryType courseCategoryType,
        CategoryType criterionCategoryType
    ) {
        if (!CategoryType.ACADEMIC_BASIC.equals(criterionCategoryType)) {
            return false;
        }
        if (admissionYear >= MAJOR_BASIC_INTRODUCED_ADMISSION_YEAR) {
            return false;
        }
        return CategoryType.MAJOR_BASIC.equals(courseCategoryType);
    }
}
