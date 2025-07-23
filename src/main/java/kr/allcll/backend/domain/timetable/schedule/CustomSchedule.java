package kr.allcll.backend.domain.timetable.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.util.List;
import kr.allcll.backend.domain.timetable.TimeTable;
import kr.allcll.backend.domain.timetable.schedule.dto.TimeSlotDto;
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

    @Column(name = "time_slots", columnDefinition = "json")
    @Convert(converter = TimeSlotListConverter.class)
    private List<TimeSlotDto> timeSlots;

    public CustomSchedule(
        TimeTable timeTable,
        String subjectName,
        String professorName,
        String location,
        List<TimeSlotDto> timeSlots
    ) {
        this.timeTable = timeTable;
        this.subjectName = subjectName;
        this.professorName = professorName;
        this.location = location;
        this.timeSlots = timeSlots;
    }

    public void updateSchedule(
        String subjectName,
        String professorName,
        String location,
        List<TimeSlotDto> timeSlots
    ) {
        if (subjectName != null) this.subjectName = subjectName;
        if (professorName != null) this.professorName = professorName;
        if (location != null) this.location = location;
        if (timeSlots != null) this.timeSlots = timeSlots;
    }
}
