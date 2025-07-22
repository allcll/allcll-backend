package kr.allcll.backend.domain.timetable.schedule;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomScheduleRepository extends JpaRepository<CustomSchedule, Long> {

    @Query("""
        SELECT cs FROM CustomSchedule cs 
        WHERE cs.timeTable.id = :timetableId 
        """)
    List<CustomSchedule> findAllByTimeTableId(@Param("timetableId") Long timetableId);

    @Query("""
        SELECT cs
        FROM CustomSchedule cs
        WHERE cs.id = :scheduleId
        AND cs.timeTable.id = :timetableId
        """)
    Optional<CustomSchedule> findByIdAndTimeTableId(
        @Param("scheduleId") Long scheduleId,
        @Param("timetableId") Long timetableId
    );

    @Modifying
    @Query("""
        DELETE FROM CustomSchedule cs 
        WHERE cs.id = :id 
        AND cs.timeTable.id = :timeTableId
        """)
    int deleteByIdAndTimeTableId(
        @Param("id") Long id,
        @Param("timeTableId") Long timeTableId);
}
