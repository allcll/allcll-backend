package kr.allcll.backend.domain.graduation.check.result.dto;

import java.util.Set;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
import kr.allcll.backend.domain.graduation.check.result.GraduationCheckCategoryResult;
import kr.allcll.backend.domain.graduation.credit.CategoryType;

public record GraduationCategory(
    MajorScope majorScope,
    CategoryType categoryType,
    Double earnedCredits,
    Integer requiredCredits,
    Double remainingCredits,
    Integer earnedAreasCnt, // 이수한 균형교양 이수구분 수
    Integer requiredAreasCnt, // 필요 군형교양 이수구분 수
    Set<BalanceRequiredArea> earnedAreas, // 이수한 균형교양 이수구분
    Boolean satisfied
) {

    public static GraduationCategory of(
        GraduationCheckCategoryResult categoryResult,
        Set<BalanceRequiredArea> earnedAreas,
        Integer requiredAreasCnt
    ) {
        if (categoryResult.getCategoryType() == CategoryType.BALANCE_REQUIRED) {
            return GraduationCategory.ofBalance(categoryResult, earnedAreas, requiredAreasCnt);
        }
        return new GraduationCategory(
            categoryResult.getMajorScope(),
            categoryResult.getCategoryType(),
            categoryResult.getMyCredits(),
            categoryResult.getRequiredCredits(),
            categoryResult.getRemainingCredits(),
            null,
            null,
            null,
            categoryResult.getIsSatisfied()
        );
    }

    public static GraduationCategory createEmptyGraduationCategory(MajorScope majorScope, CategoryType categoryType) {
        return new GraduationCategory(
            majorScope,
            categoryType,
            0.0,
            0,
            0.0,
            null,
            null,
            null,
            true
        );
    }

    public double overflowCredits() {
        return Math.max(earnedCredits - requiredCredits, 0);
    }

    public GraduationCategory withEarnedCredits(double credits) {
        double remaining = Math.max(0, requiredCredits - credits);
        boolean isSatisfied = credits >= requiredCredits;
        return new GraduationCategory(
            majorScope, categoryType, credits, requiredCredits, remaining,
            null, null, null, isSatisfied
        );
    }

    public GraduationCategory addCredits(double additional) {
        return withEarnedCredits(earnedCredits + additional);
    }

    private static GraduationCategory ofBalance(
        GraduationCheckCategoryResult balanceCategory,
        Set<BalanceRequiredArea> earnedAreas,
        Integer requiredAreasCnt
    ) {
        return new GraduationCategory(
            balanceCategory.getMajorScope(),
            balanceCategory.getCategoryType(),
            balanceCategory.getMyCredits(),
            balanceCategory.getRequiredCredits(),
            balanceCategory.getRemainingCredits(),
            earnedAreas.size(),
            requiredAreasCnt,
            earnedAreas,
            balanceCategory.getIsSatisfied()
        );
    }
}
