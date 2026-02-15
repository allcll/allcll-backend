package kr.allcll.backend.domain.graduation.certification;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassicCertCriterionRepository extends JpaRepository<ClassicCertCriterion, Long> {

    Optional<ClassicCertCriterion> findByAdmissionYear(Integer admissionYear);
}
