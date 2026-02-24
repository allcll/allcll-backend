package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.credit.CategoryType;

public class CompletedCourseFixture {

    public static CompletedCourseDto createCompletedCourse(String curiNo, String grade) {
        return new CompletedCourseDto(
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
