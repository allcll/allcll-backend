package kr.allcll.backend.domain.graduation.certification;

import java.util.Set;
import lombok.Getter;

@Getter
public enum GraduationCertRuleType { // 졸업인증 정책 종류
    BOTH_REQUIRED(
        Set.of(
            GraduationCertType.CERT_ENGLISH,
            GraduationCertType.CERT_CLASSIC
        ),
        2
    ),
    TWO_OF_THREE(
        Set.of(
            GraduationCertType.CERT_ENGLISH,
            GraduationCertType.CERT_CLASSIC,
            GraduationCertType.CERT_CODING
        ),
        2
    );

    private final Set<GraduationCertType> GraduationCertTypes;
    private final int requiredPassCount;

    GraduationCertRuleType(Set<GraduationCertType> GraduationCertTypes, int requiredPassCount) {
        this.GraduationCertTypes = GraduationCertTypes;
        this.requiredPassCount = requiredPassCount;
    }

    public int calculatePassedCount(boolean isEnglishPassed, boolean isClassicPassed, boolean isCodingPassed) {
        int count = 0;
        if (GraduationCertTypes.contains(GraduationCertType.CERT_ENGLISH) && isEnglishPassed) {
            count++;
        }
        if (GraduationCertTypes.contains(GraduationCertType.CERT_CLASSIC) && isClassicPassed) {
            count++;
        }
        if (GraduationCertTypes.contains(GraduationCertType.CERT_CODING) && isCodingPassed) {
            count++;
        }
        return count;
    }

    public boolean isSatisfied(int passedCount) {
        return passedCount >= requiredPassCount;
    }
}
