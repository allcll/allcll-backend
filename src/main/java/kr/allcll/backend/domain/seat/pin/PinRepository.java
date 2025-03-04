package kr.allcll.backend.domain.seat.pin;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.domain.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PinRepository extends JpaRepository<Pin, Long> {

    @Query("select p from Pin p join fetch p.subject s "
        + "where p.token = :tokenId "
        + "and s.deletedAt is null "
        + "and s.isDeleted = false")
    List<Pin> findAllByToken(String tokenId);

    @Query("select p from Pin p "
        + "where p.subject = :subject "
        + "and p.token = :token")
    Optional<Pin> findBySubjectAndTokenToDelete(Subject subject, String token);

    @Query("select case when COUNT(p) > 0 then true else false end from Pin p join p.subject s "
        + "where p.subject = :subject "
        + "and p.token = :token "
        + "and s.deletedAt is null "
        + "and s.isDeleted = false")
    boolean existsBySubjectAndToken(Subject subject, String token);

    @Query("select count(p) from Pin p join p.subject s "
        + "where p.token = :token "
        + "and s.isDeleted = false "
        + "and s.deletedAt is null")
    Long countAllByToken(String token);
}
