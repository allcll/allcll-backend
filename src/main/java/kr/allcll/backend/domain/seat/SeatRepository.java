package kr.allcll.backend.domain.seat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.allcll.backend.domain.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query(""" 
        select s from Seat s
        join s.subject sub
        where s.subject = :subject
        and s.createdDate = :today
        and sub.isDeleted = false
        and sub.semesterAt = :semesterAt
        """)
    Optional<Seat> findBySubjectAndCreatedDate(Subject subject, LocalDate today, String semesterAt);

    @Query("""
        select s from Seat s
        join s.subject sub
        where s.createdDate = :createdDate
        and sub.isDeleted = false
        and sub.semesterAt = :semesterAt
        """)
    List<Seat> findAllByCreatedDate(LocalDate createdDate, String semesterAt);
}
