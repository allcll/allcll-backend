package kr.allcll.backend.domain.graduation.check.cert.dto;

public record GraduationCertInfo(
    boolean isEnglishCertPassed,   // 영어 인증 통과 여부
    boolean isCodingCertPassed,  // 코딩 인증 통과 여부
    boolean isClassicCertPassed,  // 고전독서 인증 통과 여부
    int classicsTotalRequiredCount,  // 전체 요구 권수
    int classicsTotalMyCount,  // 내 총 인증 권수
    int requiredCountWestern,  // 서양의 역사와 사상 요구 권수
    int myCountWestern,  // 서양의 역사와 사상 내 인증 권수
    int requiredCountEastern,  // 동양의 역사와 사상 요구 권수
    int myCountEastern,  // 동양의 역사와 사상 내 인증 권수
    int requiredCountEasternAndWestern,  // 동서양의 문학 요구 권수
    int myCountEasternAndWestern,  // 동서양의 문학 내 인증 권수
    int requiredCountScience,  // 과학 사상 요구 권수
    int myCountScience  // 과학 사상 내 인증 권수
) {

    public static GraduationCertInfo of(
        boolean englishPass,
        boolean codingPass,
        boolean classicsPass,
        ClassicsCounts classics
    ) {
        return new GraduationCertInfo(
            englishPass,
            codingPass,
            classicsPass,
            classics.classicsTotalRequiredCount(),
            classics.classicsTotalMyCount(),
            classics.requiredCountWestern(),
            classics.myCountWestern(),
            classics.requiredCountEastern(),
            classics.myCountEastern(),
            classics.requiredCountEasternAndWestern(),
            classics.myCountEasternAndWestern(),
            classics.requiredCountScience(),
            classics.myCountScience()
        );
    }
}
