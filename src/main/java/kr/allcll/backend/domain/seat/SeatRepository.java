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

    @Query("select s from Seat s join s.subject sub "
        + "where s.subject = :subject "
        + "and s.createdDate = :today "
        + "and sub.deletedAt is null "
        + "and sub.isDeleted = false")
    Optional<Seat> findBySubjectAndCreatedDate(Subject subject, LocalDate today);

    @Query("select s from Seat s "
        + "join s.subject sub "
        + "where s.createdDate = :createdDate "
        + "and sub.deletedAt is null "
        + "and sub.isDeleted = false")
    List<Seat> findAllByCreatedDate(LocalDate createdDate);
}
