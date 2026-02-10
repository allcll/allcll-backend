package kr.allcll.backend.domain.graduation.balance;

import java.util.List;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceRequiredAreaExclusionRepository extends JpaRepository<BalanceRequiredAreaExclusion, Long> {

    List<BalanceRequiredAreaExclusion> findByAdmissionYearAndDeptGroup(
        Integer admissionYear,
        DeptGroup deptGroup
    );
}
