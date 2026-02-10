package kr.allcll.backend.domain.graduation.balance;

import java.util.Optional;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BalanceRequiredAreaExclusionRepository extends JpaRepository<BalanceRequiredAreaExclusion, Long> {

    @Query("""
            select e from BalanceRequiredAreaExclusion e
            where e.admissionYear = :admissionYear
            and e.deptGroup = :deptGroup
        """)
    Optional<BalanceRequiredAreaExclusion> findByAdmissionYearAndDeptGroup(Integer admissionYear, DeptGroup deptGroup);
}
