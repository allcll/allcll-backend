package kr.allcll.backend.support.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(value = {BaseEntityListener.class})
public abstract class BaseEntity {

    String semesterAt;
    LocalDateTime createdAt;
}
