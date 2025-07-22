package kr.allcll.backend.domain.timetable.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.time.LocalTime;
import kr.allcll.backend.domain.timetable.TimeTable;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "custom_schedule")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomSchedule extends BaseEntity {

    @Id
    @Column(name = "id")
    @TableGenerator(
        name = "schedule_id_generator",
        table = "id_generator",
        pkColumnName = "gen_name",
        valueColumnName = "gen_val",
        pkColumnValue = "schedule",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "schedule_id_generator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id")
    private TimeTable timeTable;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "professor_name")
    private String professorName;

    @Column(name = "location")
    private String location;

    @Column(name = "day_of_weeks")
    private String dayOfWeeks;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    public CustomSchedule(
        TimeTable timeTable,
        String subjectName,
        String professorName,
        String location,
        String dayOfWeeks,
        LocalTime startTime,
        LocalTime endTime
    ) {
        this.timeTable = timeTable;
        this.subjectName = subjectName;
        this.professorName = professorName;
        this.location = location;
        this.dayOfWeeks = dayOfWeeks;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void updateSchedule(
        String subjectName,
        String professorName,
        String location,
        String dayOfWeeks,
        LocalTime startTime,
        LocalTime endTime
    ) {
        if (subjectName != null) {
            this.subjectName = subjectName;
        }
        if (professorName != null) {
            this.professorName = professorName;
        }
        if (location != null) {
            this.location = location;
        }
        if (dayOfWeeks != null) {
            this.dayOfWeeks = dayOfWeeks;
        }
        if (startTime != null) {
            this.startTime = startTime;
        }
        if (endTime != null) {
            this.endTime = endTime;
        }
    }
}
