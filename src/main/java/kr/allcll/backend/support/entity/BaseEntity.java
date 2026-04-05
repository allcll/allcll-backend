package kr.allcll.backend.support.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(value = {BaseEntityListener.class})
public abstract class BaseEntity {

    protected String semesterAt;
    protected LocalDateTime createdAt;
    protected LocalDateTime deletedAt;
    protected boolean isDeleted;

    void setSemesterAtIfAbsent(String semesterAt) {
        if (this.semesterAt == null) {
            this.semesterAt = semesterAt;
        }
    }

    void setCreatedAtIfAbsent(LocalDateTime createdAt) {
        if (this.createdAt == null) {
            this.createdAt = createdAt;
        }
    }

    protected void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
