package kr.allcll.backend.domain.graduation.check.result;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GraduationCheckRepository extends JpaRepository<GraduationCheck, Long> {

    @Query("""
        select g
        from GraduationCheck g
        where g.userId = :userId
    """)
    Optional<GraduationCheck> findByUserId(Long userId);
}
