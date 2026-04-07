package kr.allcll.backend.support.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import kr.allcll.backend.support.semester.Semester;

public class BaseEntityListener {

    @PrePersist
    public void initialize(BaseEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setSemesterAtIfAbsent(getCurrentSemester());
        entity.setCreatedAtIfAbsent(now);
        entity.setUpdatedAtIfAbsent(now);
    }

    @PreUpdate
    public void update(BaseEntity entity) {
        entity.updateUpdatedAt(LocalDateTime.now());
    }

    private String getCurrentSemester() {
        Semester semester = Semester.findByDate(LocalDate.now());
        return semester.getKoreanName();
    }
}
