package kr.allcll.backend.domain.graduation.check.cert;

import jakarta.persistence.EntityManager;
import kr.allcll.backend.domain.graduation.certification.ClassicCertCriterion;
import kr.allcll.backend.domain.graduation.certification.ClassicCertCriterionRepository;
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
    private final ClassicCertCriterionRepository classicCertCriterionRepository;
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

        // 고전독서 기준 데이터 DB에서 조회
        ClassicCertCriterion classicCriteria = classicCertCriterionRepository
            .findByAdmissionYear(user.getAdmissionYear())
            .orElseThrow(
                () -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_RULE_NOT_FOUND, user.getAdmissionYear()));

        int requiredCountWestern = classicCriteria.getRequiredCountWestern();
        int requiredCountEastern = classicCriteria.getRequiredCountEastern();
        int requiredCountEasternAndWestern = classicCriteria.getRequiredCountEasternAndWestern();
        int requiredCountScience = classicCriteria.getRequiredCountScience();
        int classicsTotalRequiredCount = classicCriteria.getTotalRequiredCount();

        boolean isWesternSatisfied = certInfo.myCountWestern() >= requiredCountWestern;
        boolean isEasternSatisfied = certInfo.myCountEastern() >= requiredCountEastern;
        boolean isEasternAndWesternSatisfied = certInfo.myCountEasternAndWestern() >= requiredCountEasternAndWestern;
        boolean isScienceSatisfied = certInfo.myCountScience() >= requiredCountScience;

        GraduationCheckCertResult existingResult = graduationCheckCertResultRepository.findById(user.getId())
            .orElse(null);

        if (existingResult != null) {
            existingResult.update(
                certRuleType, passedCount, requiredPassCount, isSatisfied,
                certInfo,
                classicsTotalRequiredCount,
                requiredCountWestern, isWesternSatisfied,
                requiredCountEastern, isEasternSatisfied,
                requiredCountEasternAndWestern, isEasternAndWesternSatisfied,
                requiredCountScience, isScienceSatisfied
            );
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
                classicsTotalRequiredCount,
                certInfo.classicsTotalMyCount(),
                requiredCountWestern,
                certInfo.myCountWestern(),
                isWesternSatisfied,
                requiredCountEastern,
                certInfo.myCountEastern(),
                isEasternSatisfied,
                requiredCountEasternAndWestern,
                certInfo.myCountEasternAndWestern(),
                isEasternAndWesternSatisfied,
                requiredCountScience,
                certInfo.myCountScience(),
                isScienceSatisfied
            );
            entityManager.persist(newResult);
        }
    }
}