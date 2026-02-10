package kr.allcll.backend.domain.graduation.balance;

import java.util.List;
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
}
