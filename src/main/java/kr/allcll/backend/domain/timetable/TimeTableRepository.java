package kr.allcll.backend.domain.timetable;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {

    @Query("""
        SELECT t FROM TimeTable t 
        WHERE t.token = :token
        """)
    List<TimeTable> findAllByToken(@Param("token") String token);

    @Query("""
        SELECT t FROM TimeTable t 
        WHERE t.id = :id
        """)
    Optional<TimeTable> findById(@Param("id") Long id);

    @Query("""
        SELECT t FROM TimeTable t 
        WHERE t.id = :id 
        AND t.token = :token
        """)
    Optional<TimeTable> findByIdAndToken(@Param("id") Long id, @Param("token") String token);
}
