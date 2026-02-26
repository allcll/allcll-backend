package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DoubleCreditCriterionRepository extends JpaRepository<DoubleCreditCriterion, Long> {

    @Query("""
            select d from DoubleCreditCriterion d
            where d.admissionYear = :admissionYear
            and (d.primaryDeptCd = :primaryDeptCd or d.secondaryDeptCd = :secondaryDeptCd)
        """)
    List<DoubleCreditCriterion> findByAdmissionYearAndDeptCds(
        Integer admissionYear,
        String primaryDeptCd,
        String secondaryDeptCd
    );

    @Query("""
            select d from DoubleCreditCriterion d
            where d.admissionYear = :admissionYear
            and d.majorType = :majorType
            and d.primaryDeptCd = :primaryDeptCd
            and d.secondaryDeptCd = :secondaryDeptCd
        """)
    List<DoubleCreditCriterion> findByPair(
        Integer admissionYear,
        MajorType majorType,
        String primaryDeptCd,
        String secondaryDeptCd
    );
}
