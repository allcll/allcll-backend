package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CreditCriterionRepository extends JpaRepository<CreditCriterion, Long> {

    @Query("""
        select c from CreditCriterion c
        where c.admissionYear = :admissionYear
        and c.majorType = :majorType
        and c.deptCd = :deptCd
    """)
    List<CreditCriterion> findNonMajorCriteria(
        Integer admissionYear,
        MajorType majorType,
        String deptCd
    );

    @Query("""
        select c from CreditCriterion c
        where c.admissionYear = :admissionYear
        and c.majorType = :majorType
        and c.deptCd in :deptCds
    """)
    List<CreditCriterion> findMajorCriteriaCandidates(
        Integer admissionYear,
        MajorType majorType,
        List<String> deptCds
    );
}
