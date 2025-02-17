package kr.allcll.seatfinder.pin;

import java.util.List;
import java.util.Optional;
import kr.allcll.seatfinder.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PinRepository extends JpaRepository<Pin, Long> {

    @Query("select p from Pin p join fetch p.subject where p.token = :tokenId")
    List<Pin> findAllByToken(String tokenId);

    Optional<Pin> findBySubjectAndToken(Subject subject, String token);

    boolean existsBySubjectAndToken(Subject subject, String token);

    Long countAllByToken(String token);
}
