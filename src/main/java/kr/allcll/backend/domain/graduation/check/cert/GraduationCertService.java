package kr.allcll.backend.domain.graduation.check.cert;

import jakarta.persistence.EntityManager;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRule;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleRepository;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import kr.allcll.backend.domain.graduation.check.cert.dto.GraduationCertInfo;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GraduationCertService {

    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;
    private final GraduationCertRuleRepository graduationCertRuleRepository;
    private final EntityManager entityManager;

    @Transactional
    public void createOrUpdate(User user, GraduationCertInfo certInfo) {
        GraduationCertRule certRule = graduationCertRuleRepository.findByAdmissionYear(user.getAdmissionYear())
            .orElseThrow(
                () -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_RULE_NOT_FOUND, user.getAdmissionYear()));
        GraduationCertRuleType certRuleType = certRule.getGraduationCertRuleType();

        int passedCount = certRuleType.calculatePassedCount(
            certInfo.isEnglishCertPassed(),
            certInfo.isClassicCertPassed(),
            certInfo.isCodingCertPassed()
        );
        int requiredPassCount = certRuleType.getRequiredPassCount();
        boolean isSatisfied = certRuleType.isSatisfied(passedCount);

        GraduationCheckCertResult existingResult = graduationCheckCertResultRepository.findById(user.getId())
            .orElse(null);

        if (existingResult != null) {
            existingResult.update(certRuleType, passedCount, requiredPassCount, isSatisfied, certInfo);
        } else {
            GraduationCheckCertResult newResult = new GraduationCheckCertResult(
                user,
                certRuleType,
                passedCount,
                requiredPassCount,
                isSatisfied,
                certInfo.isEnglishCertPassed(),
                certInfo.isCodingCertPassed(),
                certInfo.isClassicCertPassed(),
                certInfo.classicsTotalRequiredCount(),
                certInfo.classicsTotalMyCount(),
                certInfo.requiredCountWestern(),
                certInfo.myCountWestern(),
                certInfo.isWesternSatisfied(),
                certInfo.requiredCountEastern(),
                certInfo.myCountEastern(),
                certInfo.isEasternSatisfied(),
                certInfo.requiredCountEasternAndWestern(),
                certInfo.myCountEasternAndWestern(),
                certInfo.isEasternAndWesternSatisfied(),
                certInfo.requiredCountScience(),
                certInfo.myCountScience(),
                certInfo.isScienceSatisfied()
            );
            entityManager.persist(newResult);
        }
    }
}
