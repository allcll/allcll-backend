package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CreditCriterionRepository extends JpaRepository<CreditCriterion, Long> {

    @Query("""
            select c
            from CreditCriterion c
            where c.admissionYear = :admissionYear
            and c.deptNm = :deptNm
            and c.majorType in :majorTypes
        """)
    List<CreditCriterion> findByAdmissionYearAndDeptNmAndMajorTypeIn(
        Integer admissionYear,
        String deptNm,
        List<MajorType> majorTypes
    );

    @Query("""
            select c
            from CreditCriterion c
            where c.admissionYear = :admissionYear
            and c.majorType = :majorType
        """)
    List<CreditCriterion> findByAdmissionYearAndMajorType(
        Integer admissionYear,
        MajorType majorType
    );

    @Query("""
            select c from CreditCriterion c
            where c.admissionYear = :admissionYear
            and c.majorType = :majorType
            and c.deptCd = :deptCd
        """)
    List<CreditCriterion> findByAdmissionYearAndMajorTypeAndDeptCd(
        Integer admissionYear,
        MajorType majorType,
        String deptCd
    );
}
