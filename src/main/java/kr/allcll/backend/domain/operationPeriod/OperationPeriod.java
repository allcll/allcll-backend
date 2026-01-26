package kr.allcll.backend.domain.operationPeriod;

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
public class OperationPeriod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    Semester semester;

    @Enumerated(EnumType.STRING)
    OperationType operationType;

    LocalDateTime startDate;

    LocalDateTime endDate;

    String message;

    private OperationPeriod(Semester semester, OperationType operationType,
        LocalDateTime startDate, LocalDateTime endDate, String message) {
        this.semester = semester;
        this.operationType = operationType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.message = message;
    }

    public static OperationPeriod create(
        Semester semester,
        OperationType operationType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String message
    ) {
        return new OperationPeriod(semester, operationType, startDate, endDate, message);
    }

    public String getSemesterKoreanName() {
        return semester.getKoreanName();
    }

    public void update(LocalDateTime startDate, LocalDateTime endDate, String message) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.message = message;
    }
}
