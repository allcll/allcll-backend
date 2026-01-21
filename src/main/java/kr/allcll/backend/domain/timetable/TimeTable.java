package kr.allcll.backend.domain.timetable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.allcll.backend.support.entity.BaseEntity;
import kr.allcll.backend.support.semester.Semester;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeTable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private String timeTableName;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    public TimeTable(String token, String timeTableName, Semester semester) {
        this.token = token;
        this.timeTableName = timeTableName;
        this.semester = semester;
        this.semesterAt = semester.getValue();
    }

    public void updateTimeTable(String newTitle) {
        this.timeTableName = newTitle;
    }
}
