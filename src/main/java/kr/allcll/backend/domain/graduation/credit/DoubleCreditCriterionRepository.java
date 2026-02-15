package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DoubleCreditCriterionRepository extends JpaRepository<DoubleCreditCriterion, Long> {

    @Query("SELECT d FROM DoubleCreditCriterion d WHERE d.admissionYear = :admissionYear "
        + "AND (d.primaryDeptCd = :primaryDeptCd OR d.secondaryDeptCd = :secondaryDeptCd)")
    List<DoubleCreditCriterion> findByAdmissionYearAndDeptCds(
        Integer admissionYear,
        String primaryDeptCd,
        String secondaryDeptCd
    );
}
