package kr.allcll.backend.domain.graduation.credit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredResolver;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoryResponse;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NonMajorCategoryResolver {

    private final RequiredCourseResolver requiredCourseResolver;
    private final BalanceRequiredResolver balanceRequiredResolver;
    private final CreditCriterionRepository creditCriterionRepository;
    private final GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;

    public List<GraduationCategoryResponse> resolve(Integer admissionYear, String deptCd) {
        List<CreditCriterion> creditCriteria =
            creditCriterionRepository.findByAdmissionYearAndMajorTypeAndDeptCd(admissionYear, MajorType.ALL, deptCd);

        Map<CategoryType, List<RequiredCourseResponse>> requiredCoursesByCategory =
            requiredCourseResolver.resolveRequiredCourses(admissionYear, deptCd);

        List<GraduationCategoryResponse> graduationCategoryResponses = new ArrayList<>();
        for (CreditCriterion creditCriterion : creditCriteria) {
            CategoryType categoryType = creditCriterion.getCategoryType();

            graduationCategoryResponses.add(GraduationCategoryResponse.of(
                MajorScope.PRIMARY,
                creditCriterion.getCategoryType(),
                creditCriterion.getEnabled(),
                creditCriterion.getRequiredCredits(),
                requiredCoursesByCategory.getOrDefault(categoryType, List.of())
            ));
        }

        GraduationDepartmentInfo graduationDepartmentInfo = graduationDepartmentInfoRepository
            .findByAdmissionYearAndDeptCd(admissionYear, deptCd)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.DEPARTMENT_NOT_FOUND));

        balanceRequiredResolver.resolve(admissionYear, deptCd, graduationDepartmentInfo.getDeptGroup())
            .ifPresent(graduationCategoryResponses::add);

        return graduationCategoryResponses;
    }

}
