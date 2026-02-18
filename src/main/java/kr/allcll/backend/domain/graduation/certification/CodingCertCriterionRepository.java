package kr.allcll.backend.domain.graduation.certification;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CodingCertCriterionRepository extends JpaRepository<CodingCertCriterion, Long> {

    @Query("""
        select c from CodingCertCriterion c
        where c.admissionYear = :admissionYear
        and c.codingTargetType = :codingTargetType
        """)
    Optional<CodingCertCriterion> findCodingCertCriterionForTarget(int admissionYear, CodingTargetType codingTargetType);
}
