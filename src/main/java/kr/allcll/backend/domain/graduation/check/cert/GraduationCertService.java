package kr.allcll.backend.domain.graduation.check.cert;

import kr.allcll.backend.domain.graduation.certification.GraduationCertRule;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleRepository;
import kr.allcll.backend.domain.graduation.check.cert.dto.GraduationCertInfo;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduationCertService {

    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;
    private final GraduationCertRuleRepository graduationCertRuleRepository;

    public void createOrUpdate(User user, GraduationCertInfo certInfo) {
        int admissionYear = user.getAdmissionYear();
        GraduationCertRule certRule = graduationCertRuleRepository.findByAdmissionYear(admissionYear);
        int passedCount = getPassedCount(certInfo.englishPass(), certInfo.classicsPass(), certInfo.codingPass());
        int requiredPassCount = certRule.getRequiredPassCount();

        boolean isTotalSatisfied = isSatisfied(passedCount, requiredPassCount);
        boolean classicsDomain1Satisfied = isSatisfied(certInfo.classicsDomain1MyCount(),
            certInfo.classicsDomain1RequiredCount());
        boolean classicsDomain2Satisfied = isSatisfied(certInfo.classicsDomain2MyCount(),
            certInfo.classicsDomain2RequiredCount());
        boolean classicsDomain3Satisfied = isSatisfied(certInfo.classicsDomain3MyCount(),
            certInfo.classicsDomain3RequiredCount());
        boolean classicsDomain4Satisfied = isSatisfied(certInfo.classicsDomain4MyCount(),
            certInfo.classicsDomain4RequiredCount());

        GraduationCheckCertResult result =
            new GraduationCheckCertResult(
                user,
                certRule.getGraduationCertRuleType(),
                passedCount,
                requiredPassCount,
                isTotalSatisfied,
                certInfo.englishPass(),
                certInfo.codingPass(),
                certInfo.classicsPass(),
                certInfo.classicsTotalRequiredCount(),
                certInfo.classicsTotalMyCount(),
                certInfo.classicsDomain1RequiredCount(),
                certInfo.classicsDomain1MyCount(),
                classicsDomain1Satisfied,
                certInfo.classicsDomain2RequiredCount(),
                certInfo.classicsDomain2MyCount(),
                classicsDomain2Satisfied,
                certInfo.classicsDomain3RequiredCount(),
                certInfo.classicsDomain3MyCount(),
                classicsDomain3Satisfied,
                certInfo.classicsDomain4RequiredCount(),
                certInfo.classicsDomain4MyCount(),
                classicsDomain4Satisfied
            );

        graduationCheckCertResultRepository.save(result);
    }

    private int getPassedCount(boolean isEnglishPass, boolean isClassicPass, boolean isCodingPass) {
        int passedCount = 0;
        if (isEnglishPass) {
            passedCount += 1;
        }
        if (isClassicPass) {
            passedCount += 1;
        }
        if (isCodingPass) {
            passedCount += 1;
        }
        return passedCount;
    }

    private Boolean isSatisfied(int myCount, int requiredCount) {
        if (myCount >= requiredCount) {
            return true;
        }
        return false;
    }
}
