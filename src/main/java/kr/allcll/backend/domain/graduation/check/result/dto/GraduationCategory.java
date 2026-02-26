package kr.allcll.backend.domain.graduation.check.result.dto;

import java.util.Set;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
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

}
