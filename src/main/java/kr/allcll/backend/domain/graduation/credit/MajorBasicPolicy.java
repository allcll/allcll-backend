package kr.allcll.backend.domain.graduation.credit;

import java.util.Objects;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import org.springframework.stereotype.Component;

@Component
public class MajorBasicPolicy {

    private static final int MAJOR_BASIC_INTRODUCED_ADMISSION_YEAR = 2024;

    public boolean matchesCriterionCategory(
        int admissionYear,
        CompletedCourse completedCourse,
        CategoryType criterionCategoryType
    ) {
        CategoryType courseCategoryType = completedCourse.getCategoryType();

        if (Objects.equals(courseCategoryType, criterionCategoryType)) {
            return true;
        }

        return shouldCountMajorBasicAsAcademicBasic(admissionYear, courseCategoryType, criterionCategoryType);
    }

    public CompletedCourse normalizeForAcademicBasic(
        int admissionYear,
        CompletedCourse completedCourse,
        CategoryType criterionCategoryType
    ) {
        CategoryType courseCategoryType = completedCourse.getCategoryType();

        if (shouldCountMajorBasicAsAcademicBasic(admissionYear, courseCategoryType, criterionCategoryType)) {
            return completedCourse.updateAcademicBasic();
        }

        return completedCourse;
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
