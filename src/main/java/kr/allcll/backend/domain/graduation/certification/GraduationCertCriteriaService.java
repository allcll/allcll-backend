package kr.allcll.backend.domain.graduation.certification;

import kr.allcll.backend.domain.graduation.MajorType;
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
import kr.allcll.backend.domain.user.UserService;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GraduationCertCriteriaService {

    private final UserService userService;
    private final GraduationCertRuleRepository graduationCertRuleRepository;
    private final CodingCertCriterionRepository codingCertCriterionRepository;
    private final GraduationDepartmentInfoRepository departmentInfoRepository;
    private final ClassicCertCriterionRepository classicCertCriterionRepository;
    private final EnglishCertCriterionRepository englishCertCriterionRepository;


    public GraduationCertCriteriaResponse getGraduationCertCriteria(Long userId) {
        User user = userService.getById(userId);
        int admissionYear = user.getAdmissionYear();

        GraduationDepartmentInfo primaryDeptInfo = findDepartment(admissionYear, user.getDeptCd());
        GraduationDepartmentInfo doubleDeptInfo = findDoubleDepartmentIfExists(admissionYear, user);

        EnglishTargetType resolvedEnglishTargetType = MajorTargetTypeResolver.resolveEnglishTargetType(primaryDeptInfo, doubleDeptInfo);
        CodingTargetType resolvedCodingTargetType = MajorTargetTypeResolver.resolveCodingTargetType(primaryDeptInfo, doubleDeptInfo);

        GraduationCertRule graduationCertRule = graduationCertRuleRepository.findByAdmissionYear(admissionYear)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_RULE_NOT_FOUND, String.valueOf(admissionYear)));

        GraduationCertCriteriaTargetResponse criteriaTarget = buildCriteriaTarget(resolvedEnglishTargetType, resolvedCodingTargetType);
        GraduationCertPolicyResponse certPolicy = buildCertPolicy(graduationCertRule.getGraduationCertRuleType(), resolvedEnglishTargetType, resolvedCodingTargetType);
        EnglishCertCriteriaResponse englishCriteria = buildEnglishCriteria(admissionYear, resolvedEnglishTargetType);
        ClassicCertCriteriaResponse classicCriteria = buildClassicCriteria(admissionYear);
        CodingCertCriteriaResponse codingCriteria = buildCodingCriteria(admissionYear, resolvedCodingTargetType);

        return GraduationCertCriteriaResponse.of(criteriaTarget, certPolicy, englishCriteria, classicCriteria,
            codingCriteria);
    }

    private GraduationDepartmentInfo findDepartment(int admissionYear, String deptCd) {
        return departmentInfoRepository.findByAdmissionYearAndDeptCd(admissionYear, deptCd)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND, deptCd));
    }

    private GraduationDepartmentInfo findDoubleDepartmentIfExists(int admissionYear, User user) {
        if (MajorType.SINGLE.equals(user.getMajorType())) {
            return null;
        }
        return findDepartment(admissionYear, user.getDoubleDeptCd());
    }

    private GraduationCertCriteriaTargetResponse buildCriteriaTarget(EnglishTargetType englishTargetType, CodingTargetType codingTargetType) {
        return GraduationCertCriteriaTargetResponse.of(
            englishTargetType.name(),
            codingTargetType.name()
        );
    }

    private GraduationCertPolicyResponse buildCertPolicy(GraduationCertRuleType certRuleType, EnglishTargetType englishTargetType,
        CodingTargetType codingTargetType) {
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
        boolean isRequiredByRule = certRuleType.getGraduationCertTypes().contains(GraduationCertType.CERT_ENGLISH);
        boolean isTargetDept = isEnglishTarget(englishTargetType);
        return isRequiredByRule && isTargetDept;
    }

    private boolean isEnglishTarget(EnglishTargetType englishTargetType) {
        return EnglishTargetType.NON_MAJOR.equals(englishTargetType)
            || EnglishTargetType.ENGLISH_MAJOR.equals(englishTargetType);
    }

    private boolean isEnabledClassic(GraduationCertRuleType certRuleType) {
        return certRuleType.getGraduationCertTypes().contains(GraduationCertType.CERT_CLASSIC);
    }

    private boolean isEnabledCoding(GraduationCertRuleType certRuleType, CodingTargetType codingTargetType) {
        boolean isRequiredByRule = certRuleType.getGraduationCertTypes().contains(GraduationCertType.CERT_CODING);
        boolean isTargetDept = isCodingTarget(codingTargetType);
        return isRequiredByRule && isTargetDept;
    }

    private boolean isCodingTarget(CodingTargetType codingTargetType) {
        return CodingTargetType.NON_MAJOR.equals(codingTargetType)
            || CodingTargetType.CODING_MAJOR.equals(codingTargetType);
    }

    private EnglishCertCriteriaResponse buildEnglishCriteria(int admissionYear, EnglishTargetType englishTargetType) {
        if (englishTargetType == EnglishTargetType.EXEMPT) {
            return null;
        }

        EnglishCertCriterion englishCertCriterion = englishCertCriterionRepository.findByAdmissionYearAndEnglishTargetType(
                admissionYear, englishTargetType)
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
        if (codingTargetType == CodingTargetType.EXEMPT) {
            return null;
        }

        CodingCertCriterion codingCertCriterion = codingCertCriterionRepository.findByAdmissionYearAndCodingTargetType(
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
