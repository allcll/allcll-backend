package kr.allcll.backend.support.entity;

import jakarta.persistence.PrePersist;
import java.time.LocalDate;
import java.time.LocalDateTime;
import kr.allcll.backend.support.semester.Semester;

public class BaseEntityListener {

    @PrePersist
    public void initialize(BaseEntity entity) {
        entity.setSemesterAtIfAbsent(getCurrentSemester());
        entity.setCreatedAtIfAbsent(LocalDateTime.now());
    }

    private String getCurrentSemester() {
        Semester semester = Semester.findByDate(LocalDate.now());
        return semester.getKoreanName();
    }
}
