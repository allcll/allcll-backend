package kr.allcll.backend.domain.graduation.balance;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BalanceRequiredRuleRepository extends JpaRepository<BalanceRequiredRule, Long> {

    @Query("""
        select r from BalanceRequiredRule r
        where r.admissionYear = :admissionYear
        and r.deptCd in :deptCds
        and r.required = true
    """)
    Optional<BalanceRequiredRule> findRequiredRuleByAdmissionYearAndDeptCdIn(Integer admissionYear, List<String> deptCds);
}
