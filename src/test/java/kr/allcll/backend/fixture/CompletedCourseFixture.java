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
            MajorScope.PRIMARY
        );
    }
}
