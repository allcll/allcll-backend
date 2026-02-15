package kr.allcll.backend.domain.graduation.check.result;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationCheckRepository extends JpaRepository<GraduationCheck, Long> {

    Optional<GraduationCheck> findByUserId(Long userId);
}
