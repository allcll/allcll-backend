package kr.allcll.backend.domain.graduation.certification;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationCertRuleRepository extends JpaRepository<GraduationCertRule, Long> {

    Optional<GraduationCertRule> findByAdmissionYear(int admissionYear);
}
