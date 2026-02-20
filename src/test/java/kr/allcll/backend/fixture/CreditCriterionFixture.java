package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.credit.CreditCriterion;

public class CreditCriterionFixture {

    public static CreditCriterion createCriterion(CategoryType categoryType) {
        return new CreditCriterion(
            2023,
            23,
            MajorType.SINGLE,
            "소프트웨어학과",
            "3220",
            MajorScope.PRIMARY,
            categoryType,
            9,
            true,
            ""
        );
    }
}
