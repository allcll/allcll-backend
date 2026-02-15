package kr.allcll.backend.domain.graduation.check.result.dto;

public record TotalSummary(
    double totalCredits,
    int requiredCredits,
    double remainingCredits
) {

}
