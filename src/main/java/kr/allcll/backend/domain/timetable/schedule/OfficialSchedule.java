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
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.timetable.TimeTable;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "official_schedule")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OfficialSchedule extends BaseEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    public OfficialSchedule(TimeTable timeTable, Subject subject) {
        this.timeTable = timeTable;
        this.subject = subject;
    }
}
