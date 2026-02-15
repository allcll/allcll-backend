package kr.allcll.backend.domain.graduation.check.result;

import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResultRepository;
import kr.allcll.backend.domain.graduation.check.result.dto.CertResult;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CertificationChecker {

    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;

    public CertResult check(Long userId) {
        // 로그인 시 저장된 인증 정보 조회
        GraduationCheckCertResult certResult = graduationCheckCertResultRepository.findByUserId(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_NOT_FOUND));

        // 엔티티를 CertResult로 변환
        return new CertResult(
            certResult.getGraduationCertRuleType().name(),
            certResult.getPassedCount(),
            certResult.getRequiredPassCount(),
            certResult.getIsSatisfied(),
            certResult.getIsEnglishCertPassed(),
            certResult.getIsCodingCertPassed(),
            certResult.getIsClassicsCertPassed(),
            certResult.getClassicsTotalRequiredCount(),
            certResult.getClassicsTotalMyCount(),
            certResult.getRequiredCountWestern(),
            certResult.getMyCountWestern(),
            certResult.getIsClassicsWesternCertPassed(),
            certResult.getRequiredCountEastern(),
            certResult.getMyCountEastern(),
            certResult.getIsClassicsEasternCertPassed(),
            certResult.getRequiredCountEasternAndWestern(),
            certResult.getMyCountEasternAndWestern(),
            certResult.getIsClassicsEasternAndWesternCertPassed(),
            certResult.getRequiredCountScience(),
            certResult.getMyCountScience(),
            certResult.getIsClassicsScienceCertPassed()
        );
    }
}
