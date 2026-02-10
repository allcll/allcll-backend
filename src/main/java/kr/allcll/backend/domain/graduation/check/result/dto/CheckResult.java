package kr.allcll.backend.domain.graduation.check.result.dto;

import java.util.List;

public record CheckResult(
    Boolean isGraduatable,
    Double totalCredits,
    Integer requiredTotalCredits,
    Double remainingCredits,
    List<GraduationCategory> categories, // 전공 타입에 따른 영역 별 필요 이수 학점
    //List<RequiredCourseRecommendation> recommendations, // 영역 별 추천 과목
    CertResult certResult // 인증검사 결과
) {

}
