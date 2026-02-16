package kr.allcll.backend.domain.graduation.check.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.MajorScope;
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
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraduationCheckResponseMapper {

    private final GraduationCertCriteriaService graduationCertCriteriaService;
    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;
    private final GraduationCheckCategoryResultRepository graduationCheckCategoryResultRepository;

    public GraduationCheckResponse toResponseFromEntity(GraduationCheck check) {
        Long userId = check.getUserId();

        // 1. 카테고리 별  결과 조회
        List<GraduationCheckCategoryResult> categoryResults = graduationCheckCategoryResultRepository.findAllByUserId(
            userId);

        List<GraduationCategory> categories = categoryResults.stream()
            .map(result -> new GraduationCategory(
                result.getMajorScope(),
                result.getCategoryType(),
                result.getMyCredits(),
                result.getRequiredCredits(),
                result.getRemainingCredits(),
                result.getIsSatisfied()
            ))
            .toList();

        // 2. 전필 초과 시 전선으로 학점 보정
        List<GraduationCategory> adjustedCategories = adjustMajorCategories(categories);

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

    private List<GraduationCategory> adjustMajorCategories(List<GraduationCategory> graduationCategories) {
        // MAJOR_REQUIRED/MAJOR_ELECTIVE 별 그룹화
        Map<MajorScope, List<GraduationCategory>> majorByScope = graduationCategories.stream()
            .filter(this::isMajorCategory)
            .collect(Collectors.groupingBy(GraduationCategory::majorScope));

        if (majorByScope.isEmpty()) {
            return graduationCategories;
        }

        // 비전공 카테고리 추가
        List<GraduationCategory> result = new ArrayList<>();
        for (GraduationCategory category : graduationCategories) {
            if (!isMajorCategory(category)) {
                result.add(category);
            }
        }

        // scope(주전공/복수전공)별로 전필/전선 찾고 학점 adjust
        adjustMajorCreditsByScope(majorByScope, result);
        
        return result;
    }

    private boolean isMajorCategory(GraduationCategory category) {
        return CategoryType.MAJOR_REQUIRED.equals(category.categoryType())
            || CategoryType.MAJOR_ELECTIVE.equals(category.categoryType());
    }

    private void adjustMajorCreditsByScope(
        Map<MajorScope, List<GraduationCategory>> majorByScope,
        List<GraduationCategory> result
    ) {
        for (Map.Entry<MajorScope, List<GraduationCategory>> entry : majorByScope.entrySet()) {
            List<GraduationCategory> majorCategoriesByScope = entry.getValue();

            GraduationCategory majorRequiredCategory = findMajorRequired(majorCategoriesByScope);
            GraduationCategory majorElectiveCategory = findMajorElective(majorCategoriesByScope);

            // 전공(전필/전선) 이 아니면 그대로 추가
            if (majorRequiredCategory == null || majorElectiveCategory == null) {
                result.addAll(majorCategoriesByScope);
                continue;
            }

            // 초과된 전필 학점
            double majorRequiredOverflowCredits = Math.max(
                0,
                majorRequiredCategory.earnedCredits() - majorRequiredCategory.requiredCredits()
            );

            // 보정 된 전공(전필/전선) 학점 저장
            double adjustedRequiredCredits = Math.min(
                majorRequiredCategory.earnedCredits(),
                majorRequiredCategory.requiredCredits()
            );
            double adjustedElectiveCredits = majorElectiveCategory.earnedCredits() + majorRequiredOverflowCredits;

            result.add(createAdjustedMajorCategory(majorRequiredCategory, adjustedRequiredCredits));
            result.add(createAdjustedMajorCategory(majorElectiveCategory, adjustedElectiveCredits));
        }
    }

    private boolean calculateGraduatable(boolean originalGraduatable, List<GraduationCategory> categories) {
        boolean majorSatisfied = categories.stream()
            .allMatch(GraduationCategory::satisfied);
        return originalGraduatable && majorSatisfied;
    }

    private GraduationCategory findMajorRequired(List<GraduationCategory> categories) {
        for (GraduationCategory category : categories) {
            if (CategoryType.MAJOR_REQUIRED.equals(category.categoryType())) {
                return category;
            }
        }
        return null;
    }

    private GraduationCategory findMajorElective(List<GraduationCategory> categories) {
        for (GraduationCategory category : categories) {
            if (CategoryType.MAJOR_ELECTIVE.equals(category.categoryType())) {
                return category;
            }
        }
        return null;
    }

    private GraduationCategory createAdjustedMajorCategory(GraduationCategory graduationCategory,
        double adjustedCredits) {
        double remainingCredits = Math.max(0, graduationCategory.requiredCredits() - adjustedCredits);
        boolean isSatisfied = adjustedCredits >= graduationCategory.requiredCredits();

        return new GraduationCategory(
            graduationCategory.majorScope(),
            graduationCategory.categoryType(),
            adjustedCredits,
            graduationCategory.requiredCredits(),
            remainingCredits,
            isSatisfied
        );
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
        boolean isPassed = normalizePassed(isRequired, certResult.isEnglishCertPassed());
        return new EnglishCertification(
            isRequired,
            isPassed,
            englishTargetType
        );
    }

    private CodingCertification buildCodingCertification(CertResult certResult, CodingTargetType codingTargetType) {
        boolean isRequired = isRequiredCoding(certResult.ruleType(), codingTargetType);
        boolean isPassed = normalizePassed(isRequired, certResult.isCodingCertPassed());

        return new CodingCertification(
            isRequired,
            isPassed,
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

    private boolean normalizePassed(boolean isRequired, boolean isPassed) {
        if (isRequired) {
            return isPassed;
        }
        return true;
    }
}
