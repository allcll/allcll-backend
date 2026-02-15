package kr.allcll.backend.domain.graduation.balance;

import java.util.List;
import java.util.Optional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BalanceRequiredRuleRepository extends JpaRepository<BalanceRequiredRule, Long> {

    Optional<BalanceRequiredRule> findByAdmissionYearAndDeptNm(
        Integer admissionYear,
        String deptNm
    );

    Optional<BalanceRequiredRule> findByAdmissionYearAndDeptNmIsNull(
        Integer admissionYear
    );

    @Query("""
            select r from BalanceRequiredRule r
            where r.admissionYear = :admissionYear
            and r.deptCd = :deptCd
            and r.required = true
        """)
    Optional<BalanceRequiredRule> findRequiredRuleByAdmissionYearAndDeptCd(Integer admissionYear, String deptCd);

    @Query("""
            select r from BalanceRequiredRule r
            where r.admissionYear = :admissionYear
            and r.deptCd = :deptCd
            and r.required = false
        """)
    Optional<BalanceRequiredRule> findExcludedRuleByAdmissionYearAndDeptCd(Integer admissionYear, String deptCd);
}
