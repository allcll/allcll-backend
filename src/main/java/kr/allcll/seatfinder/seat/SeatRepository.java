package kr.allcll.seatfinder.seat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.allcll.seatfinder.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    Optional<Seat> findBySubjectAndCreatedDate(Subject subject, LocalDate today);

    List<Seat> findAllByCreatedDate(LocalDate createdDate);
}
