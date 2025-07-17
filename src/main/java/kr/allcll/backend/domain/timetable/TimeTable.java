package kr.allcll.backend.domain.timetable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.allcll.backend.support.entity.BaseEntity;
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

    @Column(name = "token")
    private String token;

    private String timeTableName;

    private String semester;

    public TimeTable(String token, String timeTableName, String semester) {
        this.token = token;
        this.timeTableName = timeTableName;
        this.semester = semester;
    }

    public void updateTimeTable(String newTitle) {
        this.timeTableName = newTitle;
    }
}
