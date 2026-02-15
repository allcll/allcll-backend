package kr.allcll.backend.domain.graduation.check.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;
import kr.allcll.backend.domain.graduation.certification.GraduationCertRuleType;
import kr.allcll.backend.domain.graduation.certification.GraduationCertType;
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

    private final GraduationCheckCategoryResultRepository graduationCheckCategoryResultRepository;
    private final GraduationCheckCertResultRepository graduationCheckCertResultRepository;

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
        GraduationCheckCertResult certResult = graduationCheckCertResultRepository
            .findByUserId(userId)
            .orElseThrow(() ->
                new AllcllException(AllcllErrorCode.GRADUATION_CERT_NOT_FOUND)
            );
        GraduationSummary summary = GraduationSummary.from(check);
        CertResult cert = CertResult.from(certResult);
        GraduationCertifications certifications = toCertifications(cert);

        return new GraduationCheckResponse(
            userId,
            check.getCreatedAt(),
            adjustedGraduatable,
            summary,
            adjustedCategories,
            certifications
        );
    }

    // 졸업인증 전체 기준 정보 생성
    private GraduationCertifications toCertifications(CertResult certResult) {
        return new GraduationCertifications(
            new CertificationPolicy(certResult.ruleType(), certResult.requiredPassCount()),
            certResult.passedCount(),
            certResult.requiredPassCount(),
            certResult.isSatisfied(),
            toEnglishCertification(certResult),
            toCodingCertification(certResult),
            toClassicCertification(certResult)
        );
    }

    private EnglishCertification toEnglishCertification(CertResult certResult) {
        return new EnglishCertification(
            isRequiredByCertRule(certResult.ruleType(), GraduationCertType.CERT_ENGLISH),
            certResult.isEnglishCertPassed(),
            EnglishTargetType.NON_MAJOR
        );
    }

    private CodingCertification toCodingCertification(CertResult certResult) {
        return new CodingCertification(
            isRequiredByCertRule(certResult.ruleType(), GraduationCertType.CERT_CODING),
            certResult.isCodingCertPassed(),
            CodingTargetType.CODING_MAJOR
        );
    }

    private ClassicCertification toClassicCertification(CertResult certResult) {
        return new ClassicCertification(
            isRequiredByCertRule(certResult.ruleType(), GraduationCertType.CERT_CLASSIC),
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

    private Boolean isRequiredByCertRule(String ruleTypeName, GraduationCertType certType) {
        GraduationCertRuleType ruleType = GraduationCertRuleType.valueOf(ruleTypeName);
        return ruleType.getGraduationCertTypes().contains(certType);
    }

    private List<GraduationCategory> adjustMajorCategories(List<GraduationCategory> graduationCategories) {
        // MAJOR_REQUIRED/MAJOR_ELECTIVE 별 그룹화
        Map<MajorScope, List<GraduationCategory>> majorByScope = graduationCategories.stream()
            .filter(
                category -> category.categoryType() == CategoryType.MAJOR_REQUIRED
                    || category.categoryType() == CategoryType.MAJOR_ELECTIVE
            )
            .collect(Collectors.groupingBy(GraduationCategory::majorScope));

        if (majorByScope.isEmpty()) {
            return graduationCategories;
        }

        // 비전공 카테고리 추가
        List<GraduationCategory> result = new ArrayList<>();
        for (GraduationCategory category : graduationCategories) {
            if (category.categoryType() != CategoryType.MAJOR_REQUIRED
                && category.categoryType() != CategoryType.MAJOR_ELECTIVE) {
                result.add(category);
            }
        }

        // scope(주전공/복수전공)별로 전필/전선 찾고 학점 adjust
        adjustMajorCreditsByScope(majorByScope, result);
        return result;
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
            if (category.categoryType() == CategoryType.MAJOR_REQUIRED) {
                return category;
            }
        }
        return null;
    }

    private GraduationCategory findMajorElective(List<GraduationCategory> categories) {
        for (GraduationCategory category : categories) {
            if (category.categoryType() == CategoryType.MAJOR_ELECTIVE) {
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
}
