package kr.allcll.backend.domain.graduation.check.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredAreaExclusion;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredAreaExclusionRepository;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredRule;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredRuleRepository;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCategory;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.credit.CreditCriterion;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryCreditCalculator {

    private final UserRepository userRepository;
    private final GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;
    private final BalanceRequiredRuleRepository balanceRequiredRuleRepository;
    private final BalanceRequiredAreaExclusionRepository balanceRequiredAreaExclusionRepository;

    public List<GraduationCategory> calculateCategoryResults(
        Long userId,
        List<CompletedCourseDto> completedCourses,
        List<CreditCriterion> creditCriteria
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.USER_NOT_FOUND));
        GraduationDepartmentInfo primaryDeptInfo = findPrimaryDepartmentInfo(user);
        return calculateMajor(completedCourses, primaryDeptInfo, creditCriteria);
    }

    private GraduationDepartmentInfo findPrimaryDepartmentInfo(User user) {
        int admissionYear = user.getAdmissionYear();
        String deptNm = user.getDeptNm();
        return graduationDepartmentInfoRepository
            .findByAdmissionYearAndDeptNm(admissionYear, deptNm)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND));
    }

    // 전공 학점 계산
    private List<GraduationCategory> calculateMajor(
        List<CompletedCourseDto> completedCourses,
        GraduationDepartmentInfo primaryDeptInfo,
        List<CreditCriterion> creditCriteria
    ) {
        List<GraduationCategory> results = new ArrayList<>();

        // 1. 이수구분별 학점 계산 (균형교양, 전체이수 제외)
        CreditCriterion totalCriterion = null;
        for (CreditCriterion criterion : creditCriteria) {
            if (criterion.getCategoryType() == CategoryType.BALANCE_REQUIRED) {
                continue;
            }
            if (criterion.getCategoryType() == CategoryType.TOTAL_COMPLETION) {
                totalCriterion = criterion;
                continue;
            }
            GraduationCategory category = calculateCategoryCredits(completedCourses, criterion);
            results.add(category);
        }

        // 2. 균형교양 처리(복수 전공 시, 주전공 기준)
        addBalanceRequiredIfNeeded(results, completedCourses, primaryDeptInfo);

        // 3. 전체 이수 학점
        if (totalCriterion != null) {
            results.add(
                new GraduationCategory(
                    totalCriterion.getMajorScope(),
                    CategoryType.TOTAL_COMPLETION,
                    0.0, // 이수 학점은 외부에서 계산
                    totalCriterion.getRequiredCredits(),
                    (double) totalCriterion.getRequiredCredits(),
                    false
                )
            );
        }
        return results;
    }

    // 이수한 과목들에 대한 특정 이수구분의 학점 계산
    private GraduationCategory calculateCategoryCredits(
        List<CompletedCourseDto> completedCourses,
        CreditCriterion criterion
    ) {
        double earnedCredits = completedCourses.stream()
            .filter(course -> course.categoryType() == criterion.getCategoryType())
            .filter(course -> matchesMajorScope(course, criterion.getMajorScope()))
            .mapToDouble(CompletedCourseDto::credits)
            .sum();

        int requiredCredits = criterion.getRequiredCredits();
        double remainingCredits = calculateRemainingCredits(requiredCredits, earnedCredits);
        boolean isSatisfied = remainingCredits <= 0;

        return new GraduationCategory(
            criterion.getMajorScope(),
            criterion.getCategoryType(),
            earnedCredits,
            requiredCredits,
            remainingCredits,
            isSatisfied
        );
    }

    private boolean matchesMajorScope(CompletedCourseDto course, MajorScope criterionScope) {
        if (course.majorScope() == null) {
            return MajorScope.PRIMARY.equals(criterionScope);
        }
        return course.majorScope() == criterionScope;
    }

    private void addBalanceRequiredIfNeeded(
        List<GraduationCategory> results,
        List<CompletedCourseDto> completedCourses,
        GraduationDepartmentInfo deptInfo
    ) {
        BalanceRequiredRule rule = findBalanceRequiredRule(deptInfo);
        // 균형교양이 면제인 경우
        if (rule == null || !rule.getRequired()) {
            return;
        }

        // 균형교양 학점 계산
        GraduationCategory balanceCategory = calculateBalanceRequired(
            completedCourses,
            deptInfo,
            rule
        );
        results.add(balanceCategory);
    }

    // 균형 교양 규칙 조회
    private BalanceRequiredRule findBalanceRequiredRule(GraduationDepartmentInfo deptInfo) {
        Integer admissionYear = deptInfo.getAdmissionYear();
        String deptNm = deptInfo.getDeptNm();

        // 입학년도 + 학과 기준으로 조회 시 규칙이 없으면 null 반환
        BalanceRequiredRule rule = balanceRequiredRuleRepository
            .findByAdmissionYearAndDeptNm(admissionYear, deptNm)
            .orElse(null);

        if (rule != null) {
            return rule;
        }

        // 전체(dept_nm == "ALL")규칙 조회
        return balanceRequiredRuleRepository
            .findByAdmissionYearAndDeptNm(admissionYear, "ALL")
            .orElse(null);
    }

    private GraduationCategory calculateBalanceRequired(
        List<CompletedCourseDto> completedCourses,
        GraduationDepartmentInfo deptInfo,
        BalanceRequiredRule rule
    ) {
        // 1. 제외 영역 조회
        Set<BalanceRequiredArea> excludedAreas = getExcludedAreas(deptInfo);

        // 2. 이수한 균형교양 과목의 영역 수집
        Set<BalanceRequiredArea> completedAreas = completedCourses.stream()
            .filter(course -> course.categoryType() == CategoryType.BALANCE_REQUIRED)
            .map(CompletedCourseDto::selectedArea)
            .map(BalanceRequiredArea::fromSelectedArea)
            .filter(Objects::nonNull)
            .filter(area -> !excludedAreas.contains(area))
            .collect(Collectors.toSet());

        // 3. 이수 학점 계산
        double earnedCredits = completedCourses.stream()
            .filter(course -> course.categoryType() == CategoryType.BALANCE_REQUIRED)
            .mapToDouble(CompletedCourseDto::credits)
            .sum();
        int requiredCredits = rule.getRequiredCredits();
        double remainingCredits = calculateRemainingCredits(requiredCredits, earnedCredits);

        // 4. 만족 여부: 학점 충족 && 영역 충족
        int requiredAreasCnt = rule.getRequiredAreasCnt() != null ? rule.getRequiredAreasCnt() : 0;
        boolean creditsSatisfied = earnedCredits >= requiredCredits;
        boolean areasSatisfied = completedAreas.size() >= requiredAreasCnt;
        boolean isSatisfied = creditsSatisfied && areasSatisfied;

        return new GraduationCategory(
            MajorScope.PRIMARY,
            CategoryType.BALANCE_REQUIRED,
            earnedCredits,
            requiredCredits,
            remainingCredits,
            isSatisfied
        );
    }

    private Set<BalanceRequiredArea> getExcludedAreas(GraduationDepartmentInfo deptInfo) {
        Integer admissionYear = deptInfo.getAdmissionYear();
        DeptGroup deptGroup = deptInfo.getDeptGroup();

        return balanceRequiredAreaExclusionRepository
            .findAllByAdmissionYearAndDeptGroup(admissionYear, deptGroup)
            .stream()
            .map(BalanceRequiredAreaExclusion::getBalanceRequiredArea)
            .collect(Collectors.toSet());
    }

    private double calculateRemainingCredits(int requiredCredits, double earnedCredits) {
        return Math.max(requiredCredits - earnedCredits, 0);
    }
}
