package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.credit.CourseEquivalence;

public class CourseEquivalenceFixture {

    public static CourseEquivalence createCourseEquivalence(String curiNo, String sameCourseCode) {
        return new CourseEquivalence(sameCourseCode, curiNo, "테스트과목");
    }
}
