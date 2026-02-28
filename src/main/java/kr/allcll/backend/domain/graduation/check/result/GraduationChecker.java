package kr.allcll.backend.domain.graduation.check.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.check.result.dto.CertResult;
import kr.allcll.backend.domain.graduation.check.result.dto.CheckResult;
import kr.allcll.backend.domain.graduation.check.result.dto.CriterionKey;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCategory;
import kr.allcll.backend.domain.graduation.check.result.dto.TotalSummary;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.credit.CreditCriterion;
import kr.allcll.backend.domain.graduation.credit.CreditCriterionRepository;
import kr.allcll.backend.domain.graduation.credit.DoubleCreditCriterion;
import kr.allcll.backend.domain.graduation.credit.DoubleCreditCriterionRepository;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraduationChecker {

    private final CategoryCreditCalculator categoryCalculator;
    private final CertificationChecker certificationChecker;

    private final UserRepository userRepository;
    private final CreditCriterionRepository creditCriterionRepository;
    private final DoubleCreditCriterionRepository doubleCreditCriterionRepository;

    public CheckResult calculate(Long userId, List<CompletedCourse> savedCourses) {
        List<CompletedCourse> earnedCourses = savedCourses.stream()
            .filter(CompletedCourse::isEarned)
            .toList();

        // 사용자의 졸업 요건 기준 조회
        List<CreditCriterion> creditCriteria = resolveCreditCriteria(userId);

        // 이수구분별 학점 계산
        List<GraduationCategory> categoryResults = categoryCalculator.calculateCategoryResults(
            userId,
            earnedCourses,
            creditCriteria
        );

        // 총 학점 정보 추출 (엑셀에서 직접 합산)
        TotalSummary totalSummary = summarizeTotalCredits(earnedCourses, categoryResults);

        // 졸업인증제도 검사
        CertResult certResult = certificationChecker.checkAndUpdate(userId, earnedCourses);
        boolean isGraduatable = canGraduate(categoryResults, certResult);

        return new CheckResult(
            isGraduatable,
            totalSummary.totalCredits(),
            totalSummary.requiredCredits(),
            totalSummary.remainingCredits(),
            categoryResults,
            certResult
        );
    }

    private List<CreditCriterion> resolveCreditCriteria(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
        if (MajorType.DOUBLE.equals(user.getMajorType())) {
            return buildDoubleMajorCriteria(
                user,
                user.getAdmissionYear(),
                user.getDeptNm()
            );
        }
        return creditCriterionRepository
            .findByAdmissionYearAndDeptNmAndMajorTypeIn(
                user.getAdmissionYear(),
                user.getDeptNm(),
                List.of(MajorType.ALL, MajorType.SINGLE)
            );
    }

    private List<CreditCriterion> buildDoubleMajorCriteria(
        User user,
        Integer admissionYear,
        String deptNm
    ) {
        // 교양 과목 기준 (majorType=ALL)
        List<CreditCriterion> nonMajorCriteria = creditCriterionRepository
            .findByAdmissionYearAndDeptNmAndMajorTypeIn(
                admissionYear, deptNm, List.of(MajorType.ALL));

        // (기본)전공 과목 기준 (majorType=DOUBLE)
        List<CreditCriterion> defaultDoubleCriteria = creditCriterionRepository
            .findByAdmissionYearAndMajorType(admissionYear, MajorType.DOUBLE);

        // (예외)전공 과목 기준
        List<DoubleCreditCriterion> exceptionCriteria = doubleCreditCriterionRepository
            .findByAdmissionYearAndDeptCds(
                admissionYear, user.getDeptCd(), user.getDoubleDeptCd());

        // 예외가 있으면 예외 기준으로 (기본)전공 과목 기준 대체
        List<CreditCriterion> resolvedMajorCriteria = applyExceptionCriteria(defaultDoubleCriteria, exceptionCriteria);

        List<CreditCriterion> allCriteria = new ArrayList<>(nonMajorCriteria);
        allCriteria.addAll(resolvedMajorCriteria);
        return allCriteria;
    }

    private List<CreditCriterion> applyExceptionCriteria(
        List<CreditCriterion> defaultCriteria,
        List<DoubleCreditCriterion> exceptionCriteria
    ) {
        if (exceptionCriteria.isEmpty()) {
            return defaultCriteria;
        }

        Map<CriterionKey, DoubleCreditCriterion> exceptionCriteriaByKey = toExceptionMap(exceptionCriteria);
        List<CreditCriterion> resolvedCriteria = new ArrayList<>();

        for (CreditCriterion defaultCriterion : defaultCriteria) {
            CriterionKey criterionKey = CriterionKey.from(defaultCriterion);
            DoubleCreditCriterion exceptionCriterion = exceptionCriteriaByKey.remove(criterionKey);

            if (exceptionCriterion != null) {
                resolvedCriteria.add(DoubleCreditCriterion.toCreditCriterion(exceptionCriterion));
            } else {
                resolvedCriteria.add(defaultCriterion);
            }
        }
        exceptionCriteriaByKey.values()
            .forEach(exceptionCriterion ->
                resolvedCriteria.add(DoubleCreditCriterion.toCreditCriterion(exceptionCriterion))
            );
        return resolvedCriteria;
    }

    private Map<CriterionKey, DoubleCreditCriterion> toExceptionMap(
        List<DoubleCreditCriterion> exceptionDoubleCriteria
    ) {
        Map<CriterionKey, DoubleCreditCriterion> exceptionCriteriaMap = new HashMap<>();

        for (DoubleCreditCriterion exceptionDoubleCriterion : exceptionDoubleCriteria) {
            CriterionKey key = CriterionKey.from(exceptionDoubleCriterion);
            exceptionCriteriaMap.put(key, exceptionDoubleCriterion);
        }
        return exceptionCriteriaMap;
    }

    private TotalSummary summarizeTotalCredits(
        List<CompletedCourse> earnedCourses,
        List<GraduationCategory> categories
    ) {
        double totalCredits = calculateTotalCredits(earnedCourses);

        GraduationCategory totalCategory = categories.stream()
            .filter(category -> category.categoryType() == CategoryType.TOTAL_COMPLETION)
            .findFirst()
            .orElseThrow();

        categories.remove(totalCategory);

        int requiredCredits = totalCategory.requiredCredits();
        double remainingCredits = Math.max(requiredCredits - totalCredits, 0);
        boolean satisfied = remainingCredits <= 0;

        categories.add(new GraduationCategory(
            totalCategory.majorScope(),
            CategoryType.TOTAL_COMPLETION,
            totalCredits,
            requiredCredits,
            remainingCredits,
            null,
            null,
            null,
            satisfied
        ));

        return new TotalSummary(totalCredits, requiredCredits, remainingCredits);
    }

    private double calculateTotalCredits(List<CompletedCourse> completedCourses) {
        return completedCourses.stream()
            .mapToDouble(CompletedCourse::getCredits)
            .sum();
    }

    private boolean canGraduate(
        List<GraduationCategory> categories,
        CertResult certResult
    ) {
        return categories.stream().allMatch(GraduationCategory::satisfied)
            && certResult.isSatisfied();
    }
}
