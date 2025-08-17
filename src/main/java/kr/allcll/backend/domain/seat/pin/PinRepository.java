package kr.allcll.backend.domain.seat.pin;

import java.util.List;
import java.util.Optional;
import kr.allcll.backend.domain.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PinRepository extends JpaRepository<Pin, Long> {

    @Query(""" 
        select p from Pin p
        join fetch p.subject s
        where p.token = :tokenId
        and s.isDeleted = false
        and p.semesterAt = :semesterAt
        """)
    List<Pin> findAllByToken(String tokenId, String semesterAt);


    @Query(""" 
        select p from Pin p
        join fetch p.subject s
        where s.isDeleted = false
        and p.semesterAt = :semesterAt
        """)
    List<Pin> findAllBySemesterAt(String semesterAt);

    @Query(""" 
        select p from Pin p
        join p.subject s
        where p.subject = :subject
        and p.token = :token
        and s.isDeleted = false
        and p.semesterAt = :semesterAt
        """)
    Optional<Pin> findBySubjectAndToken(Subject subject, String token, String semesterAt);

    @Query(""" 
        select case when count(p) > 0 then true else false end from Pin p
        join p.subject s
        where p.subject = :subject
        and p.token = :token
        and s.isDeleted = false
        and p.semesterAt = :semesterAt
        """)
    boolean existsBySubjectAndToken(Subject subject, String token, String semesterAt);

    @Query("""
        select count(p) from Pin p
        join p.subject s
        where p.token = :token
        and s.isDeleted = false
        and p.semesterAt = :semesterAt
        """)
    Long countAllByToken(String token, String semesterAt);
}
