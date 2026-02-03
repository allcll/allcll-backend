package kr.allcll.backend.domain.graduation.certification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationCertRuleRepository extends JpaRepository<GraduationCertRule, Long> {

    GraduationCertRule findByAdmissionYear(int admissionYear);
}
