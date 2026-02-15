package kr.allcll.backend.domain.graduation.check.cert;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GraduationCheckCertResultRepository extends JpaRepository<GraduationCheckCertResult, Long> {

    @Query("""
        select r
        from GraduationCheckCertResult r
        where r.userId = :userId
    """)
    Optional<GraduationCheckCertResult> findByUserId(Long userId);
}
