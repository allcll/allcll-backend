package kr.allcll.backend.domain.graduation.check.result.dto;

import kr.allcll.backend.domain.graduation.check.result.GraduationCheck;

public record GraduationSummary(
    Double totalMyCredits,
    Integer requiredTotalCredits,
    Double remainingCredits
) {

    public static GraduationSummary from(GraduationCheck check) {
        return new GraduationSummary(
            check.getTotalCredits(),
            check.getRequiredTotalCredits(),
            check.getRemainingCredits()
        );
    }
}
