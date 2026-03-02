package kr.allcll.backend.domain.graduation.certification;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseRepository;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassicAltCoursePolicy {

    private static final String ALT_COURSE_NAME = "고전특강";
    private static final String PASS_GRADE = "P";

    private final CompletedCourseRepository completedCourseRepository;

    public boolean isSatisfiedByAltCourse(User user) {
        List<String> altCourseGrades = completedCourseRepository
            .findCompletedCourseGrade(user.getId(), ALT_COURSE_NAME);
        return altCourseGrades.contains(PASS_GRADE);
    }
}
