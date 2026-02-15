package kr.allcll.backend.domain.graduation.certification;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClassicCertCriterionRepository extends JpaRepository<ClassicCertCriterion, Long> {

    @Query("""
        select c from ClassicCertCriterion c
        where c.admissionYear = :admissionYear
        """)
    Optional<ClassicCertCriterion> findByAdmissionYear(int admissionYear);
}
