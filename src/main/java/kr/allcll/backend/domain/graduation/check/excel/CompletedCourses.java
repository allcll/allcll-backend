package kr.allcll.backend.domain.graduation.check.excel;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class CompletedCourses {

    private final List<CompletedCourse> courses;

    public CompletedCourses(List<CompletedCourse> courses) {
        this.courses = courses;
    }

    public boolean isEmpty() {
        return courses.isEmpty();
    }

    public LocalDateTime getCourseCreatedDate() {
        if (isEmpty()) {
            return null;
        }
        return courses.getFirst().getCreatedAt();
    }
}
