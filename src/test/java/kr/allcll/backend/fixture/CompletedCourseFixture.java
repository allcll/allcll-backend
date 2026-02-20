package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.credit.CategoryType;

public class CompletedCourseFixture {

    public static CompletedCourseDto createCompletedCourse(
        CategoryType categoryType
    ) {
        return new CompletedCourseDto(
            "1234",
            "테스트과목",
            categoryType,
            "",
            3.0,
            "A+",
            MajorScope.PRIMARY
        );
    }
}
