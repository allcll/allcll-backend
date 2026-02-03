package kr.allcll.backend.domain.graduation.check.cert.dto;

public record GraduationCertInfo(
    boolean isEnglishCertPassed,   // 영어 인증 통과 여부
    boolean isCodingCertPassed,  // 코딩 인증 통과 여부
    boolean isClassicCertPassed,  // 고전독서 인증 통과 여부
    int classicsTotalRequiredCount,  // 전체 요구 권수
    int classicsTotalMyCount,  // 내 총 인증 권수
    int classicsDomain1RequiredCount,  // 서양의 역사와 사상 요구 권수
    int classicsDomain1MyCount,  // 서양의 역사와 사상 내 인증 권수
    int classicsDomain2RequiredCount,  // 동양의 역사와 사상 요구 권수
    int classicsDomain2MyCount,  // 동양의 역사와 사상 내 인증 권수
    int classicsDomain3RequiredCount,  // 동서양의 문학 요구 권수
    int classicsDomain3MyCount,  // 동서양의 문학 내 인증 권수
    int classicsDomain4RequiredCount,  // 과학 사상 요구 권수
    int classicsDomain4MyCount  // 과학 사상 내 인증 권수
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
            classics.classicsDomain1RequiredCount(),
            classics.classicsDomain1MyCount(),
            classics.classicsDomain2RequiredCount(),
            classics.classicsDomain2MyCount(),
            classics.classicsDomain3RequiredCount(),
            classics.classicsDomain3MyCount(),
            classics.classicsDomain4RequiredCount(),
            classics.classicsDomain4MyCount()
        );
    }
}
