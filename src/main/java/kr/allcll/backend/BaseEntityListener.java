package kr.allcll.backend;

import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

public class BaseEntityListener {

    @PrePersist
    public void setSemester(BaseEntity entity) {
        entity.createdAt = LocalDateTime.now();
        entity.semesterAt = Semester.now();
    }

    private static class Semester {

        private static final int MIDDLE_OF_YEAR_MONTH = 6;

        public static String now() {
            LocalDateTime now = LocalDateTime.now();
            int year = now.getYear();
            int month = now.getMonthValue();
            return String.format("%d/%s", year, calculateSemester(month));
        }

        private static String calculateSemester(int month) {
            return month <= MIDDLE_OF_YEAR_MONTH ? "1학기" : "2학기";
        }
    }
}
