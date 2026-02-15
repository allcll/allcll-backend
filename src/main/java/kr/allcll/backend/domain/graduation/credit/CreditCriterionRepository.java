package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditCriterionRepository extends JpaRepository<CreditCriterion, Long> {

    List<CreditCriterion> findByAdmissionYearAndDeptNm(
        Integer admissionYear,
        String deptNm
    );

    List<CreditCriterion> findByAdmissionYearAndDeptNmAndMajorTypeIn(
        Integer admissionYear,
        String deptNm,
        List<MajorType> majorTypes
    );

    List<CreditCriterion> findByAdmissionYearAndMajorType(
        Integer admissionYear,
        MajorType majorType
    );
}
