package kr.allcll.backend.domain.graduation.check.cert.dto;

import kr.allcll.backend.domain.graduation.check.cert.ClassicsArea;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;

public record ClassicsCounts(
    int myCountWestern,  // 서양의 역사와 사상 내 인증 권수
    int myCountEastern,  // 동양의 역사와 사상 내 인증 권수
    int myCountEasternAndWestern,  // 동서양의 문학 내 인증 권수
    int myCountScience  // 과학 사상 내 인증 권수
) {

    public int totalMyCount() {
        return ClassicsArea.WESTERN.getRecognizedCount(myCountWestern)
            + ClassicsArea.EASTERN.getRecognizedCount(myCountEastern)
            + ClassicsArea.EASTERN_AND_WESTERN.getRecognizedCount(myCountEasternAndWestern)
            + ClassicsArea.SCIENCE.getRecognizedCount(myCountScience);
    }

    public static ClassicsCounts fallback(GraduationCheckCertResult certResult) {
        return new ClassicsCounts(
            certResult.getMyCountWestern(),
            certResult.getMyCountEastern(),
            certResult.getMyCountEasternAndWestern(),
            certResult.getMyCountScience()
        );
    }

    public static ClassicsCounts empty() {
        return new ClassicsCounts(0, 0, 0, 0);
    }

    public boolean isPassed() {
        return myCountWestern >= ClassicsArea.WESTERN.getMaxRecognizedCount() &&
            myCountEastern >= ClassicsArea.EASTERN.getMaxRecognizedCount() &&
            myCountEasternAndWestern >= ClassicsArea.EASTERN_AND_WESTERN.getMaxRecognizedCount() &&
            myCountScience >= ClassicsArea.SCIENCE.getMaxRecognizedCount();
    }
}
