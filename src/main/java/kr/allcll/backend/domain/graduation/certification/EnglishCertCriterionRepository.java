package kr.allcll.backend.domain.graduation.certification;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EnglishCertCriterionRepository extends JpaRepository<EnglishCertCriterion, Long> {

    @Query("""
        select e from EnglishCertCriterion e
        where e.admissionYear = :admissionYear
        and e.englishTargetType = :englishTargetType
        """)
    Optional<EnglishCertCriterion> findEnglishCertCriterionForTarget(int admissionYear, EnglishTargetType englishTargetType);
}
