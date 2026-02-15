package kr.allcll.backend.domain.graduation.check.result.dto;

import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.credit.CreditCriterion;
import kr.allcll.backend.domain.graduation.credit.DoubleCreditCriterion;

public record CriterionKey(
    MajorScope majorScope,
    CategoryType categoryType
) {

    public static CriterionKey from(CreditCriterion criterion) {
        return new CriterionKey(criterion.getMajorScope(), criterion.getCategoryType());
    }

    public static CriterionKey from(DoubleCreditCriterion criterion) {
        return new CriterionKey(criterion.getMajorScope(), criterion.getCategoryType());
    }
}
