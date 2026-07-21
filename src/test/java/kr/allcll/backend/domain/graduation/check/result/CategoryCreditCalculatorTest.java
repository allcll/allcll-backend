package kr.allcll.backend.domain.graduation.check.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.balance.BalanceRequiredRuleRepository;
import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCategory;
import kr.allcll.backend.domain.graduation.credit.AcademicBasicPolicy;
import kr.allcll.backend.domain.graduation.credit.CategoryType;
import kr.allcll.backend.domain.graduation.credit.CreditCriterion;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfoRepository;
import kr.allcll.backend.domain.user.User;
import kr.allcll.backend.domain.user.UserRepository;
import kr.allcll.backend.fixture.GraduationDepartmentInfoFixture;
import kr.allcll.backend.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryCreditCalculatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GraduationDepartmentInfoRepository graduationDepartmentInfoRepository;

    @Mock
    private BalanceRequiredRuleRepository balanceRequiredRuleRepository;

    @Mock
    private AcademicBasicPolicy academicBasicPolicy;

    @InjectMocks
    private CategoryCreditCalculator calculator;

    @Test
    @DisplayName("2021학번 교양선택도 시트에서 내려온 학점 요건을 적용한다")
    void appliesGeneralElectiveCreditRequirementFromCriterionForAdmissionYear2021() {
        // given
        int admissionYear = 2021;
        GraduationDepartmentInfo department = GraduationDepartmentInfoFixture.createDepartmentInfo(
            admissionYear,
            CodingTargetType.NON_MAJOR
        );
        User user = UserFixture.singleMajorUser(admissionYear, department);
        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(graduationDepartmentInfoRepository.findByAdmissionYearAndDeptNm(admissionYear, department.getDeptNm()))
            .willReturn(java.util.Optional.of(department));
        given(balanceRequiredRuleRepository.findByAdmissionYearAndDeptNm(admissionYear, department.getDeptNm()))
            .willReturn(java.util.Optional.empty());
        given(balanceRequiredRuleRepository.findByAdmissionYearAndDeptNm(admissionYear, "ALL"))
            .willReturn(java.util.Optional.empty());

        CreditCriterion criterion = criterion(admissionYear, CategoryType.GENERAL_ELECTIVE, 21);

        // when
        GraduationCategory result = calculator.calculateCategoryResults(1L, List.of(), List.of(criterion)).getFirst();

        // then
        assertThat(result)
            .extracting(
                GraduationCategory::requiredCredits,
                GraduationCategory::remainingCredits,
                GraduationCategory::satisfied
            )
            .containsExactly(21, 21.0, false);
    }

    @Test
    @DisplayName("시트에서 교양선택 학점 요건을 0으로 내려주면 이수 학점과 관계없이 충족한다")
    void satisfiesGeneralElectiveWhenCriterionRequiredCreditsIsZero() {
        // given
        int admissionYear = 2021;
        GraduationDepartmentInfo department = GraduationDepartmentInfoFixture.createDepartmentInfo(
            admissionYear,
            CodingTargetType.NON_MAJOR
        );
        User user = UserFixture.singleMajorUser(admissionYear, department);
        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(graduationDepartmentInfoRepository.findByAdmissionYearAndDeptNm(admissionYear, department.getDeptNm()))
            .willReturn(java.util.Optional.of(department));
        given(balanceRequiredRuleRepository.findByAdmissionYearAndDeptNm(admissionYear, department.getDeptNm()))
            .willReturn(java.util.Optional.empty());
        given(balanceRequiredRuleRepository.findByAdmissionYearAndDeptNm(admissionYear, "ALL"))
            .willReturn(java.util.Optional.empty());

        CreditCriterion criterion = criterion(admissionYear, CategoryType.GENERAL_ELECTIVE, 0);

        // when
        GraduationCategory result = calculator.calculateCategoryResults(1L, List.of(), List.of(criterion)).getFirst();

        // then
        assertThat(result)
            .extracting(
                GraduationCategory::requiredCredits,
                GraduationCategory::remainingCredits,
                GraduationCategory::satisfied
            )
            .containsExactly(0, 0.0, true);
    }

    private CreditCriterion criterion(int admissionYear, CategoryType categoryType, int requiredCredits) {
        return new CreditCriterion(
            admissionYear,
            admissionYear % 100,
            MajorType.ALL,
            "0",
            "테스트학과",
            MajorScope.PRIMARY,
            categoryType,
            requiredCredits,
            true,
            null
        );
    }
}
