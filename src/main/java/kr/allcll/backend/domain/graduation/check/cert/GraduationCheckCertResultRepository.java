package kr.allcll.backend.domain.graduation.check.cert;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationCheckCertResultRepository extends JpaRepository<GraduationCheckCertResult, Long> {

    void deleteByUserId(Long userId);

    Optional<GraduationCheckCertResult> findByUserId(Long userId);
}
