package kr.allcll.backend.domain.graduation.check.cert.dto;

public record ClassicsCounts(
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

    public static ClassicsCounts of(int westernCertRequired, int westernCompleted, int easternCertRequired,
        int easternCompleted, int literatureCertRequired, int literatureCompleted, int scienceCertRequired,
        int scienceCompleted) {
        int totalRequired = westernCertRequired + easternCertRequired +
            literatureCertRequired + scienceCertRequired;
        int totalCompleted = westernCompleted + easternCompleted +
            literatureCompleted + scienceCompleted;
        return new ClassicsCounts(
            totalRequired,
            totalCompleted,
            westernCertRequired,
            westernCompleted,
            easternCertRequired,
            easternCompleted,
            literatureCertRequired,
            literatureCompleted,
            scienceCertRequired,
            scienceCompleted
        );
    }
}
