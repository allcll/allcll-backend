package kr.allcll.backend.domain.period;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.allcll.backend.support.entity.BaseEntity;
import kr.allcll.backend.support.semester.Semester;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "Periods")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Period extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    Semester semesterCode;

    String semesterValue;

    @Enumerated(EnumType.STRING)
    ServiceType serviceType;

    LocalDateTime startDate;

    LocalDateTime endDate;

    String message;

    public static Period create(
        Semester semesterCode,
        String semesterValue,
        ServiceType serviceType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String message
    ) {
        return new Period(null, semesterCode, semesterValue, serviceType, startDate, endDate, message);
    }
}
