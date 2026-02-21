package kr.allcll.backend.domain.graduation.certification;

import kr.allcll.backend.domain.graduation.certification.dto.ClassicCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.certification.dto.CodingCertAltCourseResponse;
import kr.allcll.backend.domain.graduation.certification.dto.CodingCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.certification.dto.EnglishCertAltCourseResponse;
import kr.allcll.backend.domain.graduation.certification.dto.EnglishCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaTargetResponse;
import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertPolicyResponse;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GraduationCertCriteriaService {

    private final UserRepository userRepository;
    private final GraduationCertRuleRepository graduationCertRuleRepository;
    private final CodingCertCriterionRepository codingCertCriterionRepository;
    private final GraduationDepartmentInfoRepository departmentInfoRepository;
    private final ClassicCertCriterionRepository classicCertCriterionRepository;
    private final EnglishCertCriterionRepository englishCertCriterionRepository;


    public GraduationCertCriteriaResponse getGraduationCertCriteria(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
        int admissionYear = user.getAdmissionYear();

        GraduationDepartmentInfo primaryDeptInfo = findDepartment(admissionYear, user.getDeptCd());

        EnglishTargetType englishTargetType = primaryDeptInfo.getEnglishTargetType();
        CodingTargetType codingTargetType = primaryDeptInfo.getCodingTargetType();

        GraduationCertRule graduationCertRule = graduationCertRuleRepository.findByAdmissionYear(admissionYear)
            .orElseThrow(() ->
                new AllcllException(AllcllErrorCode.GRADUATION_CERT_RULE_NOT_FOUND, admissionYear));

        GraduationCertCriteriaTargetResponse criteriaTarget = buildCriteriaTarget(englishTargetType, codingTargetType);
        GraduationCertPolicyResponse certPolicy =
            buildCertPolicy(graduationCertRule.getGraduationCertRuleType(), englishTargetType, codingTargetType);
        EnglishCertCriteriaResponse englishCriteria = buildEnglishCriteria(admissionYear, englishTargetType);
        ClassicCertCriteriaResponse classicCriteria = buildClassicCriteria(admissionYear);
        CodingCertCriteriaResponse codingCriteria = buildCodingCriteria(admissionYear, codingTargetType);

        return GraduationCertCriteriaResponse.of(criteriaTarget, certPolicy, englishCriteria, classicCriteria,
            codingCriteria);
    }

    private GraduationDepartmentInfo findDepartment(int admissionYear, String deptCd) {
        return departmentInfoRepository.findByAdmissionYearAndDeptCd(admissionYear, deptCd)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND, deptCd));
    }

    private GraduationCertCriteriaTargetResponse buildCriteriaTarget(
        EnglishTargetType englishTargetType,
        CodingTargetType codingTargetType
    ) {
        return GraduationCertCriteriaTargetResponse.of(
            englishTargetType.name(),
            codingTargetType.name()
        );
    }

    private GraduationCertPolicyResponse buildCertPolicy(
        GraduationCertRuleType certRuleType,
        EnglishTargetType englishTargetType,
        CodingTargetType codingTargetType
    ) {
        boolean enableEnglish = isEnabledEnglish(certRuleType, englishTargetType);
        boolean enableClassic = isEnabledClassic(certRuleType);
        boolean enableCoding = isEnabledCoding(certRuleType, codingTargetType);

        return GraduationCertPolicyResponse.of(
            certRuleType.name(),
            certRuleType.getRequiredPassCount(),
            enableEnglish,
            enableClassic,
            enableCoding
        );
    }

    private boolean isEnabledEnglish(GraduationCertRuleType certRuleType, EnglishTargetType englishTargetType) {
        boolean isRequired = certRuleType.getGraduationCertTypes().contains(GraduationCertType.CERT_ENGLISH);
        boolean isTargetDept = isEnglishTarget(englishTargetType);
        return isRequired && isTargetDept;
    }

    private boolean isEnglishTarget(EnglishTargetType englishTargetType) {
        return !isNotEnglishTarget(englishTargetType);
    }

    private boolean isNotEnglishTarget(EnglishTargetType englishTargetType) {
        return EnglishTargetType.EXEMPT.equals(englishTargetType);
    }

    private boolean isEnabledClassic(GraduationCertRuleType certRuleType) {
        return certRuleType.getGraduationCertTypes().contains(GraduationCertType.CERT_CLASSIC);
    }

    private boolean isEnabledCoding(GraduationCertRuleType certRuleType, CodingTargetType codingTargetType) {
        boolean isRequired = certRuleType.getGraduationCertTypes().contains(GraduationCertType.CERT_CODING);
        boolean isTargetDept = isCodingTarget(codingTargetType);
        return isRequired && isTargetDept;
    }

    private boolean isCodingTarget(CodingTargetType codingTargetType) {
        return !isNotCodingTarget(codingTargetType);
    }

    private boolean isNotCodingTarget(CodingTargetType codingTargetType) {
        return CodingTargetType.EXEMPT.equals(codingTargetType);
    }

    private EnglishCertCriteriaResponse buildEnglishCriteria(int admissionYear, EnglishTargetType englishTargetType) {
        if (EnglishTargetType.EXEMPT.equals(englishTargetType)) {
            return null;
        }

        EnglishCertCriterion englishCertCriterion =
            englishCertCriterionRepository.findEnglishCertCriterionForTarget(admissionYear, englishTargetType)
                .orElseThrow(() -> new AllcllException(AllcllErrorCode.ENGLISH_CERT_CRITERIA_NOT_FOUND));

        EnglishCertAltCourseResponse englishCertAltCourse = EnglishCertAltCourseResponse.of(
            englishCertCriterion.getAltCuriNo(),
            englishCertCriterion.getAltCuriNm(),
            englishCertCriterion.getAltCuriCredit()
        );

        return EnglishCertCriteriaResponse.of(
            englishCertCriterion.getEnglishTargetType().name(),
            englishCertCriterion.getToeicMinScore(),
            englishCertCriterion.getToeflIbtMinScore(),
            englishCertCriterion.getTepsMinScore(),
            englishCertCriterion.getNewTepsMinScore(),
            englishCertCriterion.getOpicMinLevel(),
            englishCertCriterion.getToeicSpeakingMinLevel(),
            englishCertCriterion.getGtelpLevel(),
            englishCertCriterion.getGtelpMinScore(),
            englishCertCriterion.getGtelpSpeakingLevel(),
            englishCertAltCourse
        );
    }

    private ClassicCertCriteriaResponse buildClassicCriteria(int admissionYear) {
        ClassicCertCriterion classicCertCriterion = classicCertCriterionRepository.findByAdmissionYear(admissionYear)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.CLASSIC_CERT_CRITERIA_NOT_FOUND));

        return ClassicCertCriteriaResponse.of(
            classicCertCriterion.getTotalRequiredCount(),
            classicCertCriterion.getRequiredCountWestern(),
            classicCertCriterion.getRequiredCountEastern(),
            classicCertCriterion.getRequiredCountEasternAndWestern(),
            classicCertCriterion.getRequiredCountScience()
        );
    }

    private CodingCertCriteriaResponse buildCodingCriteria(int admissionYear, CodingTargetType codingTargetType) {
        if (CodingTargetType.EXEMPT.equals(codingTargetType)) {
            return null;
        }

        CodingCertCriterion codingCertCriterion = codingCertCriterionRepository.findCodingCertCriterion(
                admissionYear, codingTargetType)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.CODING_CERT_CRITERIA_NOT_FOUND));

        CodingCertAltCourseResponse altCourse = CodingCertAltCourseResponse.of(
            codingCertCriterion.getAlt1CuriNo(),
            codingCertCriterion.getAlt1CuriNm(),
            codingCertCriterion.getAlt1MinGrade(),
            codingCertCriterion.getAlt2CuriNo(),
            codingCertCriterion.getAlt2CuriNm(),
            codingCertCriterion.getAlt2MinGrade()
        );

        return CodingCertCriteriaResponse.of(
            codingCertCriterion.getCodingTargetType().name(),
            codingCertCriterion.getToscMinLevel(),
            altCourse
        );
    }
}
