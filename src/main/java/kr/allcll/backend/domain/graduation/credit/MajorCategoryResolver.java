package kr.allcll.backend.domain.graduation.credit;

import java.util.ArrayList;
import java.util.List;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoryResponse;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MajorCategoryResolver {

    private static final String ALL_DEPT = "0";
    private static final String SUBJECT_MAJOR_REQUIRED = "전필";
    private static final String SUBJECT_MAJOR_ELECTIVE = "전선";

    private final SubjectRepository subjectRepository;
    private final CreditCriterionRepository creditCriterionRepository;
    private final DoubleCreditCriterionResolver doubleCreditCriterionResolver;

    public List<GraduationCategoryResponse> resolve(
        Integer admissionYear,
        MajorType majorType,
        String primaryDeptCd,
        String secondaryDeptCd,
        User user
    ) {
        if (MajorType.SINGLE.equals(majorType)) {
            return resolveSingleType(admissionYear, primaryDeptCd);
        }
        return resolveDoubleType(admissionYear, primaryDeptCd, secondaryDeptCd, user);
    }

    private List<GraduationCategoryResponse> resolveSingleType(Integer admissionYear, String deptCd) {
        List<CreditCriterion> creditCriteria =
            creditCriterionRepository.findByAdmissionYearAndMajorTypeAndDeptCd(admissionYear, MajorType.SINGLE, deptCd);

        List<GraduationCategoryResponse> graduationCategoryResponses = new ArrayList<>();
        for (CreditCriterion creditCriterion : creditCriteria) {
            CategoryType categoryType = creditCriterion.getCategoryType();
            if (categoryType.isNonMajorCategory()) {
                continue;
            }

            graduationCategoryResponses.add(GraduationCategoryResponse.of(
                MajorScope.PRIMARY,
                creditCriterion.getCategoryType(),
                creditCriterion.getEnabled(),
                creditCriterion.getRequiredCredits(),
                loadMajorSubjects(deptCd, creditCriterion.getCategoryType())
            ));
        }
        return graduationCategoryResponses;
    }

    private List<GraduationCategoryResponse> resolveDoubleType(
        Integer admissionYear,
        String primaryDeptCd,
        String secondaryDeptCd,
        User user
    ) {
        List<DoubleCreditCriterion> doubleCreditCriteria = doubleCreditCriterionResolver.resolve(user);
        if (!doubleCreditCriteria.isEmpty()) {
            return buildFromDoubleCreditCriteria(doubleCreditCriteria, primaryDeptCd, secondaryDeptCd);
        }
        List<CreditCriterion> fallbackCriteria =
            creditCriterionRepository.findByAdmissionYearAndMajorTypeAndDeptCd(admissionYear, MajorType.DOUBLE, ALL_DEPT);
        return buildFromCreditCriteriaFallback(fallbackCriteria, primaryDeptCd, secondaryDeptCd);
    }

    private List<GraduationCategoryResponse> buildFromDoubleCreditCriteria(
        List<DoubleCreditCriterion> doubleCreditCriteria,
        String primaryDeptCd,
        String secondaryDeptCd
    ) {
        List<GraduationCategoryResponse> graduationCategoryResponses = new ArrayList<>();

        for (DoubleCreditCriterion doubleCreditCriterion : doubleCreditCriteria) {
            CategoryType categoryType = doubleCreditCriterion.getCategoryType();
            if (categoryType.isNonMajorCategory()) {
                continue;
            }

            String deptCd = resolveDeptCdByScope(doubleCreditCriterion.getMajorScope(), primaryDeptCd, secondaryDeptCd);

            graduationCategoryResponses.add(GraduationCategoryResponse.of(
                doubleCreditCriterion.getMajorScope(),
                categoryType,
                doubleCreditCriterion.getEnabled(),
                doubleCreditCriterion.getRequiredCredits(),
                loadMajorSubjects(deptCd, categoryType)
            ));
        }

        return graduationCategoryResponses;
    }

    private String resolveDeptCdByScope(MajorScope majorScope, String primaryDeptCd, String secondaryDeptCd) {
        if (MajorScope.PRIMARY.equals(majorScope)) {
            return primaryDeptCd;
        }
        return secondaryDeptCd;
    }

    private List<GraduationCategoryResponse> buildFromCreditCriteriaFallback(
        List<CreditCriterion> creditCriteria,
        String primaryDeptCd,
        String secondaryDeptCd
    ) {
        List<GraduationCategoryResponse> graduationCategoryResponses = new ArrayList<>();

        for (CreditCriterion creditCriterion : creditCriteria) {
            CategoryType categoryType = creditCriterion.getCategoryType();
            if (categoryType.isNonMajorCategory()) {
                continue;
            }

            String deptCd = resolveDeptCdByScope(creditCriterion.getMajorScope(), primaryDeptCd, secondaryDeptCd);

            graduationCategoryResponses.add(GraduationCategoryResponse.of(
                creditCriterion.getMajorScope(),
                categoryType,
                creditCriterion.getEnabled(),
                creditCriterion.getRequiredCredits(),
                loadMajorSubjects(deptCd, categoryType)
            ));
        }

        return graduationCategoryResponses;
    }

    private List<RequiredCourseResponse> loadMajorSubjects(String deptCd, CategoryType categoryType) {
        String curiTypeCdNm = resolveCuriTypeCdNm(categoryType);

        List<Subject> majorSubjects = subjectRepository.findByDeptCdAndCuriTypeCdNm(deptCd, curiTypeCdNm);
        return majorSubjects.stream()
            .map(majorSubject -> RequiredCourseResponse.of(majorSubject.getCuriNo(), majorSubject.getCuriNm()))
            .toList();
    }

    private String resolveCuriTypeCdNm(CategoryType categoryType) {
        if (CategoryType.MAJOR_REQUIRED.equals(categoryType)) {
            return SUBJECT_MAJOR_REQUIRED;
        }
        return SUBJECT_MAJOR_ELECTIVE;
    }
}
