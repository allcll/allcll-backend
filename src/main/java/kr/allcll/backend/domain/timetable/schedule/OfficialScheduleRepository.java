package kr.allcll.backend.domain.timetable.schedule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficialScheduleRepository extends JpaRepository<OfficialSchedule, Long> {

    @Query("""
        SELECT os FROM OfficialSchedule os
        WHERE os.timeTable.id = :timetableId
        """)
    List<OfficialSchedule> findAllByTimeTableId(@Param("timetableId") Long timetableId);

    @Modifying
    @Query("""
        DELETE FROM OfficialSchedule os
        WHERE os.id = :id
        AND os.timeTable.id = :timeTableId
        """)
    int deleteByIdAndTimeTableId(
        @Param("id") Long id,
        @Param("timeTableId") Long timeTableId);

    @Query("""
        SELECT CASE WHEN COUNT(os) > 0 THEN TRUE ELSE FALSE END
        FROM OfficialSchedule os
        WHERE os.timeTable.id = :timetableId
        AND os.subject.id = :subjectId
        """)
    boolean existsByTimeTableIdAndSubjectId(
        @Param("timetableId") Long timetableId,
        @Param("subjectId") Long subjectId
    );
}
