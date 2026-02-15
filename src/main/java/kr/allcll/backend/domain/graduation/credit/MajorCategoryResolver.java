package kr.allcll.backend.domain.graduation.credit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.graduation.MajorScope;
import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.credit.dto.GraduationCategoryResponse;
import kr.allcll.backend.domain.graduation.credit.dto.RequiredCourseResponse;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

        Map<CategoryType, List<RequiredCourseResponse>> subjectsByCategory = loadMajorSubjects(deptCd);

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
                subjectsByCategory.getOrDefault(categoryType, List.of())
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
            creditCriterionRepository.findByAdmissionYearAndMajorTypeAndDeptCd(admissionYear, MajorType.DOUBLE,
                ALL_DEPT);
        return buildFromCreditCriteriaFallback(fallbackCriteria, primaryDeptCd, secondaryDeptCd);
    }

    private List<GraduationCategoryResponse> buildFromDoubleCreditCriteria(
        List<DoubleCreditCriterion> doubleCreditCriteria,
        String primaryDeptCd,
        String secondaryDeptCd
    ) {
        List<GraduationCategoryResponse> graduationCategoryResponses = new ArrayList<>();

        Map<CategoryType, List<RequiredCourseResponse>> primarySubjectsByCategory =
            loadMajorSubjects(primaryDeptCd);
        Map<CategoryType, List<RequiredCourseResponse>> secondarySubjectsByCategory =
            loadMajorSubjects(secondaryDeptCd);

        for (DoubleCreditCriterion doubleCreditCriterion : doubleCreditCriteria) {
            CategoryType categoryType = doubleCreditCriterion.getCategoryType();
            if (categoryType.isNonMajorCategory()) {
                continue;
            }

            Map<CategoryType, List<RequiredCourseResponse>> subjectsByCategory = resolveSubjectsByScope(
                doubleCreditCriterion.getMajorScope(),
                primarySubjectsByCategory,
                secondarySubjectsByCategory
            );

            graduationCategoryResponses.add(GraduationCategoryResponse.of(
                doubleCreditCriterion.getMajorScope(),
                categoryType,
                doubleCreditCriterion.getEnabled(),
                doubleCreditCriterion.getRequiredCredits(),
                subjectsByCategory.getOrDefault(categoryType, List.of())
            ));
        }

        return graduationCategoryResponses;
    }

    private List<GraduationCategoryResponse> buildFromCreditCriteriaFallback(
        List<CreditCriterion> creditCriteria,
        String primaryDeptCd,
        String secondaryDeptCd
    ) {
        List<GraduationCategoryResponse> graduationCategoryResponses = new ArrayList<>();

        Map<CategoryType, List<RequiredCourseResponse>> primarySubjectsByCategory =
            loadMajorSubjects(primaryDeptCd);
        Map<CategoryType, List<RequiredCourseResponse>> secondarySubjectsByCategory =
            loadMajorSubjects(secondaryDeptCd);

        for (CreditCriterion creditCriterion : creditCriteria) {
            CategoryType categoryType = creditCriterion.getCategoryType();
            if (categoryType.isNonMajorCategory()) {
                continue;
            }

            Map<CategoryType, List<RequiredCourseResponse>> subjectsByCategory = resolveSubjectsByScope(
                creditCriterion.getMajorScope(),
                primarySubjectsByCategory,
                secondarySubjectsByCategory
            );
            graduationCategoryResponses.add(GraduationCategoryResponse.of(
                creditCriterion.getMajorScope(),
                categoryType,
                creditCriterion.getEnabled(),
                creditCriterion.getRequiredCredits(),
                subjectsByCategory.getOrDefault(categoryType, List.of())
            ));
        }

        return graduationCategoryResponses;
    }

    private Map<CategoryType, List<RequiredCourseResponse>> loadMajorSubjects(String deptCd) {
        Map<CategoryType, List<RequiredCourseResponse>> majorSubjects = new HashMap<>();
        majorSubjects.put(CategoryType.MAJOR_REQUIRED, loadMajorSubjectsByCuriType(deptCd, SUBJECT_MAJOR_REQUIRED));
        majorSubjects.put(CategoryType.MAJOR_ELECTIVE, loadMajorSubjectsByCuriType(deptCd, SUBJECT_MAJOR_ELECTIVE));
        return majorSubjects;
    }

    private List<RequiredCourseResponse> loadMajorSubjectsByCuriType(String deptCd, String curiTypeCdNm) {
        List<Subject> majorSubjects = subjectRepository.findByDeptCdAndCuriTypeCdNm(deptCd, curiTypeCdNm);
        return majorSubjects.stream()
            .collect(
                Collectors.toMap(
                    Subject::getCuriNo,
                    subject -> RequiredCourseResponse.of(subject.getCuriNo(), subject.getCuriNm()),
                    (existing, duplicated) -> existing
                )
            )
            .values()
            .stream()
            .toList();
    }

    private Map<CategoryType, List<RequiredCourseResponse>> resolveSubjectsByScope(
        MajorScope majorScope,
        Map<CategoryType, List<RequiredCourseResponse>> primarySubjectsByCategory,
        Map<CategoryType, List<RequiredCourseResponse>> secondarySubjectsByCategory
    ) {
        if (MajorScope.PRIMARY.equals(majorScope)) {
            return primarySubjectsByCategory;
        }
        return secondarySubjectsByCategory;
    }
}
