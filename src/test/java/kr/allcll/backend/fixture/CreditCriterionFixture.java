package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.credit.CreditCriterion;

public class CreditCriterionFixture {

    public static CreditCriterion createAcademicBasicCriterion(String deptNm, Integer admissionYear) {
        return new CreditCriterion(
            admissionYear,
            admissionYear % 100,
            MajorType.SINGLE,
            "3210",
            deptNm,
            MajorScope.PRIMARY,
            CategoryType.ACADEMIC_BASIC,
            9,
            true,
            ""
        );
    }
}
