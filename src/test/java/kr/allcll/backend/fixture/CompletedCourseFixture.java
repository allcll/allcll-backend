package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.credit.CategoryType;

public class CompletedCourseFixture {

    public static CompletedCourse createCompletedCourse(Long userId, String curiNo, String grade) {
        return new CompletedCourse(
            userId,
            curiNo,
            "테스트과목",
            CategoryType.GENERAL_ELECTIVE,
            "",
            3.0,
            grade,
            MajorScope.PRIMARY,
            true
        );
    }

    public static CompletedCourse createCompletedCourse(CategoryType categoryType) {
        return new CompletedCourse(
            1L,
            "1234",
            "테스트과목",
            categoryType,
            "",
            3.0,
            "A+",
            MajorScope.PRIMARY,
            true
        );
    }
}
