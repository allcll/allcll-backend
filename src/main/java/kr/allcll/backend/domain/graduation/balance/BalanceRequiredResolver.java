package kr.allcll.backend.domain.graduation.balance;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.allcll.backend.domain.graduation.balance.dto.BalanceAreaCoursesResponse;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoryResponse;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceRequiredResolver {

    private static final String ALL_DEPT = "0";

    private final BalanceRequiredRuleRepository balanceRequiredRuleRepository;
    private final BalanceRequiredAreaExclusionRepository balanceRequiredAreaExclusionRepository;
    private final BalanceRequiredCourseAreaMapRepository balanceRequiredCourseAreaMapRepository;

    public Optional<GraduationCategoryResponse> resolve(Integer admissionYear, String deptCd, DeptGroup deptGroup) {
        Optional<BalanceRequiredRule> balanceRequiredRuleOpt =
            balanceRequiredRuleRepository.findExcludedRuleByAdmissionYearAndDeptCd(admissionYear, deptCd)
                .or(() -> balanceRequiredRuleRepository.findRequiredRuleByAdmissionYearAndDeptCd(admissionYear, ALL_DEPT));

        if (balanceRequiredRuleOpt.isEmpty()) {
            return Optional.empty();
        }

        BalanceRequiredRule balanceRequiredRule = balanceRequiredRuleOpt.get();

        if (Boolean.FALSE.equals(balanceRequiredRule.getRequired())) {
            return Optional.of(
                GraduationCategoryResponse.balanceRequiredOf(
                    CategoryType.BALANCE_REQUIRED,
                    false,
                    balanceRequiredRule.getRequiredCredits(),
                    balanceRequiredRule.getRequiredAreasCnt(),
                    List.of(),
                    null
                )
            );
        }

        BalanceRequiredAreaExclusion balanceRequiredAreaExclusion =
            balanceRequiredAreaExclusionRepository.findByAdmissionYearAndDeptGroup(admissionYear, deptGroup)
                .orElseThrow(() -> new AllcllException(AllcllErrorCode.BALANCE_REQUIRED_EXCLUSION_NOT_FOUND));

        BalanceRequiredArea excludedArea = balanceRequiredAreaExclusion.getBalanceRequiredArea();

        List<BalanceAreaCoursesResponse> balanceAreaCoursesResponses = loadAreaToCourses(admissionYear, excludedArea);

        return Optional.of(
            GraduationCategoryResponse.balanceRequiredOf(
                CategoryType.BALANCE_REQUIRED,
                true,
                balanceRequiredRule.getRequiredCredits(),
                balanceRequiredRule.getRequiredAreasCnt(),
                balanceAreaCoursesResponses,
                excludedArea
            )
        );
    }

    private List<BalanceAreaCoursesResponse> loadAreaToCourses(
        Integer admissionYear,
        BalanceRequiredArea excludedBalanceRequiredArea
    ) {
        Map<BalanceRequiredArea, List<RequiredCourseResponse>> coursesByArea =
            balanceRequiredCourseAreaMapRepository.findAllByAdmissionYear(admissionYear)
                .stream()
                .filter(courseAreaMap -> courseAreaMap.getBalanceRequiredArea() != excludedBalanceRequiredArea)
                .collect(groupingBy(
                    BalanceRequiredCourseAreaMap::getBalanceRequiredArea,
                    mapping(courseAreaMap -> RequiredCourseResponse.of(courseAreaMap.getCuriNo(), courseAreaMap.getCuriNm()), toList())
                ));

        return coursesByArea.entrySet().stream()
            .map(areaCourse -> BalanceAreaCoursesResponse.of(areaCourse.getKey(), areaCourse.getValue()))
            .toList();
    }
}
