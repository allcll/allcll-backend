package kr.allcll.backend.domain.graduation.check.result;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredRule;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredRuleRepository;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;
import kr.allcll.backend.domain.graduation.certification.GraduationCertCriteriaService;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import kr.allcll.backend.domain.graduation.certification.GraduationCertType;
import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaResponse;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResultRepository;
import kr.allcll.backend.domain.graduation.check.result.dto.CertResult;
import kr.allcll.backend.domain.graduation.check.result.dto.CertificationPolicy;
import kr.allcll.backend.domain.graduation.check.result.dto.ClassicCertification;
import kr.allcll.backend.domain.graduation.check.result.dto.ClassicDomainRequirement;
import kr.allcll.backend.domain.graduation.check.result.dto.CodingCertification;
import kr.allcll.backend.domain.graduation.check.result.dto.EnglishCertification;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCategory;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCertifications;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCheckResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationSummary;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraduationCheckResponseMapper {

    private final GraduationCertCriteriaService graduationCertCriteriaService;
    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;
    private final GraduationCheckBalanceAreaResultRepository graduationCheckBalanceAreaResultRepository;
    private final BalanceRequiredRuleRepository balanceRequiredRuleRepository;
    private final UserRepository userRepository;
    private final GraduationCheckCategoryResultRepository graduationCheckCategoryResultRepository;

    public GraduationCheckResponse toResponseFromEntity(GraduationCheck check) {
        Long userId = check.getUserId();

        // 1. 카테고리 별  결과 조회
        List<GraduationCheckCategoryResult> categoryResults = graduationCheckCategoryResultRepository.findAllByUserId(
            userId);

        // 균형교양 이수 영역 조회
        Set<BalanceRequiredArea> earnedAreas = graduationCheckBalanceAreaResultRepository.findAllByUserId(userId)
            .stream()
            .map(GraduationCheckBalanceAreaResult::getBalanceRequiredArea)
            .collect(Collectors.toSet());

        // 균형교양 필요 영역 수 조회
        Integer requiredAreasCnt = findRequiredAreasCnt(userId);

        List<GraduationCategory> categories = categoryResults.stream()
            .map(result -> GraduationCategory.of(result, earnedAreas, requiredAreasCnt))
            .toList();

        // 2. 전필 초과 시 전선으로, 전선 초과 시 교양으로 학점 보정
        List<GraduationCategory> adjustedCategories = reallocateCredits(categories);

        boolean adjustedGraduatable = calculateGraduatable(
            check.getCanGraduate(),
            adjustedCategories
        );

        // 3. 졸업인증 결과 조회
        GraduationCheckCertResult certResult = graduationCheckCertResultRepository.findByUserId(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.GRADUATION_CERT_NOT_FOUND));
        CertResult cert = CertResult.from(certResult);

        GraduationSummary summary = GraduationSummary.from(check);

        GraduationCertCriteriaResponse certCriteria = graduationCertCriteriaService.getGraduationCertCriteria(userId);
        EnglishTargetType englishTargetType = parseEnglishTargetType(certCriteria);
        CodingTargetType codingTargetType = parseCodingTargetType(certCriteria);
        GraduationCertifications certifications = buildCertifications(cert, englishTargetType, codingTargetType);

        return new GraduationCheckResponse(
            userId,
            check.getCreatedAt(),
            adjustedGraduatable,
            summary,
            adjustedCategories,
            certifications
        );
    }

    private Integer findRequiredAreasCnt(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        BalanceRequiredRule rule = balanceRequiredRuleRepository
            .findByAdmissionYearAndDeptNm(user.getAdmissionYear(), user.getDeptNm())
            .orElse(null);
        if (rule == null) {
            rule = balanceRequiredRuleRepository
                .findByAdmissionYearAndDeptNm(user.getAdmissionYear(), "ALL")
                .orElse(null);
        }

        if (rule == null || !rule.getRequired()) {
            return null;
        }

        return rule.getRequiredAreasCnt();
    }

    private List<GraduationCategory> reallocateCredits(List<GraduationCategory> graduationCategories) {
        Map<MajorScope, List<GraduationCategory>> groupedReallocateTargets = graduationCategories.stream()
            .filter(this::isReallocateTarget)
            .collect(groupingBy(GraduationCategory::majorScope));

        if (groupedReallocateTargets.isEmpty()) {
            return graduationCategories;
        }

        List<GraduationCategory> finishReallocate = new ArrayList<>();
        for (GraduationCategory category : graduationCategories) {
            if (!isReallocateTarget(category)) {
                finishReallocate.add(category);
            }
        }

        adjustReallocateTargetCredits(groupedReallocateTargets, finishReallocate);

        return finishReallocate;
    }

    private void adjustReallocateTargetCredits(
        Map<MajorScope, List<GraduationCategory>> groupedReallocateTargets,
        List<GraduationCategory> finishReallocate
    ) {
        for (Map.Entry<MajorScope, List<GraduationCategory>> entry : groupedReallocateTargets.entrySet()) {
            MajorScope majorScope = entry.getKey();
            List<GraduationCategory> reallocateCategory = entry.getValue();

            GraduationCategory majorRequired = findCategoryOrEmpty(reallocateCategory, CategoryType.MAJOR_REQUIRED, majorScope);
            GraduationCategory majorElective = findCategoryOrEmpty(reallocateCategory, CategoryType.MAJOR_ELECTIVE, majorScope);
            GraduationCategory general = findCategoryOrEmpty(reallocateCategory, CategoryType.GENERAL, majorScope);

            boolean hasMajorRequired = findCategory(reallocateCategory, CategoryType.MAJOR_REQUIRED) != null;
            boolean hasMajorElective = findCategory(reallocateCategory, CategoryType.MAJOR_ELECTIVE) != null;
            boolean hasGeneral = findCategory(reallocateCategory, CategoryType.GENERAL) != null;

            double overMajorRequired = majorRequired.overflowCredits();
            GraduationCategory adjustedRequired = majorRequired.withEarnedCredits(
                Math.min(majorRequired.earnedCredits(), majorRequired.requiredCredits())
            );

            GraduationCategory electiveWithCarry = majorElective.addCredits(overMajorRequired);
            double overMajorElective;
            GraduationCategory adjustedElective;
            if (hasMajorElective) {
                overMajorElective = electiveWithCarry.overflowCredits();
                adjustedElective = electiveWithCarry.withEarnedCredits(
                    Math.min(electiveWithCarry.earnedCredits(), majorElective.requiredCredits())
                );
            } else {
                overMajorElective = 0;
                adjustedElective = electiveWithCarry;
            }

            GraduationCategory adjustedGeneral = general.addCredits(overMajorElective);

            if (hasMajorRequired || adjustedRequired.earnedCredits() > 0) {
                finishReallocate.add(adjustedRequired);
            }
            if (hasMajorElective || overMajorRequired > 0) {
                finishReallocate.add(adjustedElective);
            }
            if (hasGeneral || overMajorElective > 0) {
                finishReallocate.add(adjustedGeneral);
            }
        }
    }

    private GraduationCategory findCategoryOrEmpty(
        List<GraduationCategory> categories,
        CategoryType categoryType,
        MajorScope majorScope
    ) {
        GraduationCategory found = findCategory(categories, categoryType);
        return found != null ? found : GraduationCategory.createEmptyGraduationCategory(majorScope, categoryType);
    }

    private boolean isReallocateTarget(GraduationCategory graduationCategory) {
        return graduationCategory.categoryType().isReallocateTarget();
    }

    private boolean calculateGraduatable(boolean originalGraduatable, List<GraduationCategory> categories) {
        boolean majorSatisfied = categories.stream()
            .allMatch(GraduationCategory::satisfied);
        return originalGraduatable && majorSatisfied;
    }

    private GraduationCategory findCategory(
        List<GraduationCategory> categories,
        CategoryType categoryType
    ) {
        return categories.stream()
            .filter(category -> category.categoryType() == categoryType)
            .findFirst()
            .orElse(null);
    }

    private EnglishTargetType parseEnglishTargetType(GraduationCertCriteriaResponse response) {
        String typeName = response.criteriaTarget().englishTargetType();
        return EnglishTargetType.valueOf(typeName);
    }

    private CodingTargetType parseCodingTargetType(GraduationCertCriteriaResponse response) {
        String typeName = response.criteriaTarget().codingTargetType();
        return CodingTargetType.valueOf(typeName);
    }

    // 졸업인증 전체 기준 정보 생성
    private GraduationCertifications buildCertifications(
        CertResult certResult,
        EnglishTargetType englishTargetType,
        CodingTargetType codingTargetType
    ) {
        CertificationPolicy policy = new CertificationPolicy(certResult.ruleType(), certResult.requiredPassCount());
        EnglishCertification english = buildEnglishCertification(certResult, englishTargetType);
        CodingCertification coding = buildCodingCertification(certResult, codingTargetType);
        ClassicCertification classic = buildClassicCertification(certResult);
        return new GraduationCertifications(
            policy,
            certResult.passedCount(),
            certResult.requiredPassCount(),
            certResult.isSatisfied(),
            english,
            coding,
            classic
        );
    }

    private EnglishCertification buildEnglishCertification(CertResult certResult, EnglishTargetType englishTargetType) {
        boolean isRequired = isRequiredEnglish(certResult.ruleType(), englishTargetType);
        return new EnglishCertification(
            isRequired,
            certResult.isEnglishCertPassed(),
            englishTargetType
        );
    }

    private CodingCertification buildCodingCertification(CertResult certResult, CodingTargetType codingTargetType) {
        boolean isRequired = isRequiredCoding(certResult.ruleType(), codingTargetType);
        return new CodingCertification(
            isRequired,
            certResult.isCodingCertPassed(),
            codingTargetType
        );
    }

    private ClassicCertification buildClassicCertification(CertResult certResult) {
        return new ClassicCertification(
            isRequiredClassic(certResult.ruleType()),
            certResult.isClassicsCertPassed(),
            certResult.classicsTotalRequiredCount(),
            certResult.classicsTotalMyCount(),
            List.of(
                new ClassicDomainRequirement(
                    "WESTERN_HISTORY_THOUGHT",
                    certResult.requiredCountWestern(),
                    certResult.myCountWestern(),
                    certResult.isClassicsWesternCertPassed()
                ),
                new ClassicDomainRequirement(
                    "EASTERN_HISTORY_THOUGHT",
                    certResult.requiredCountEastern(),
                    certResult.myCountEastern(),
                    certResult.isClassicsEasternCertPassed()
                ),
                new ClassicDomainRequirement(
                    "EAST_WEST_LITERATURE",
                    certResult.requiredCountEasternAndWestern(),
                    certResult.myCountEasternAndWestern(),
                    certResult.isClassicsEasternAndWesternCertPassed()
                ),
                new ClassicDomainRequirement(
                    "SCIENCE_THOUGHT",
                    certResult.requiredCountScience(),
                    certResult.myCountScience(),
                    certResult.isClassicsScienceCertPassed()
                )
            )
        );
    }

    private boolean isRequiredEnglish(String ruleTypeName, EnglishTargetType englishTargetType) {
        GraduationCertRuleType ruleType = GraduationCertRuleType.valueOf(ruleTypeName);
        boolean isRequiredByPolicy = ruleType.getGraduationCertTypes().contains(GraduationCertType.CERT_ENGLISH);
        boolean isTargetDept = isEnglishTarget(englishTargetType);
        return isRequiredByPolicy && isTargetDept;
    }

    private boolean isEnglishTarget(EnglishTargetType englishTargetType) {
        return !EnglishTargetType.EXEMPT.equals(englishTargetType);
    }

    private boolean isRequiredCoding(String ruleTypeName, CodingTargetType codingTargetType) {
        GraduationCertRuleType ruleType = GraduationCertRuleType.valueOf(ruleTypeName);
        boolean isRequiredByPolicy = ruleType.getGraduationCertTypes().contains(GraduationCertType.CERT_CODING);
        boolean isTargetDept = isCodingTarget(codingTargetType);
        return isRequiredByPolicy && isTargetDept;
    }

    private boolean isCodingTarget(CodingTargetType codingTargetType) {
        return !CodingTargetType.EXEMPT.equals(codingTargetType);
    }

    private boolean isRequiredClassic(String ruleTypeName) {
        GraduationCertRuleType ruleType = GraduationCertRuleType.valueOf(ruleTypeName);
        return ruleType.getGraduationCertTypes().contains(GraduationCertType.CERT_CLASSIC);
    }
}
