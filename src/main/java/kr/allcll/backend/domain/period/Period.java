package kr.allcll.backend.domain.period;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
    Semester semester;

    @Enumerated(EnumType.STRING)
    ServiceType serviceType;

    LocalDateTime startDate;

    LocalDateTime endDate;

    String message;

    private Period(Semester semester, ServiceType serviceType,
        LocalDateTime startDate, LocalDateTime endDate, String message) {
        this.semester = semester;
        this.serviceType = serviceType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.message = message;
    }

    public static Period create(
        Semester semester,
        ServiceType serviceType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String message
    ) {
        return new Period(semester, serviceType, startDate, endDate, message);
    }

    public String getSemesterValue() {
        return semester.getValue();
    }

    public void update(LocalDateTime startDate, LocalDateTime endDate, String message) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.message = message;
    }
}
