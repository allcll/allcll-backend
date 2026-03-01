package kr.allcll.backend.domain.graduation.certification;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CodingAltCoursePolicy implements GraduationCertificationAltCoursePolicy{

    private final CodingCertCriterionRepository codingCertCriterionRepository;

    @Override
    public boolean isSatisfiedByAltCourse(
        User user,
        GraduationDepartmentInfo departmentInfo,
        List<CompletedCourse> earnedCourses
    ) {
        CodingTargetType codingTargetType = departmentInfo.getCodingTargetType();
        if (isExempt(codingTargetType)) {
            return false;
        }

        return codingCertCriterionRepository
            .findCodingCertCriterion(user.getAdmissionYear(), codingTargetType)
            .filter(codingCertCriterion -> isAltCourseCompletedByTargetType(
                    codingTargetType,
                    earnedCourses,
                    codingCertCriterion
                )
            )
            .isPresent();
    }

    private boolean isExempt(CodingTargetType codingTargetType) {
        return CodingTargetType.EXEMPT.equals(codingTargetType);
    }

    private boolean isAltCourseCompletedByTargetType(
        CodingTargetType codingTargetType,
        List<CompletedCourse> earnedCourses,
        CodingCertCriterion codingCertCriterion
    ) {
        if (CodingTargetType.CODING_MAJOR.equals(codingTargetType)) {
            return isAltCourseCompletedForCodingMajor(earnedCourses, codingCertCriterion);
        }
        if (CodingTargetType.NON_MAJOR.equals(codingTargetType)) {
            return isAltCourseCompletedForNonMajor(earnedCourses, codingCertCriterion);
        }
        return false;
    }

    private boolean isAltCourseCompletedForCodingMajor(
        List<CompletedCourse> earnedCourses,
        CodingCertCriterion codingCertCriterion
    ) {
        String alt1CuriNo = codingCertCriterion.getAlt1CuriNo();
        String alt1MinGrade = codingCertCriterion.getAlt1MinGrade();
        return isAltCourseCompleted(earnedCourses, alt1CuriNo, alt1MinGrade);
    }

    private boolean isAltCourseCompletedForNonMajor(
        List<CompletedCourse> earnedCourses,
        CodingCertCriterion codingCertCriterion
    ) {
        String alt1CuriNo = codingCertCriterion.getAlt1CuriNo();
        String alt1MinGrade = codingCertCriterion.getAlt1MinGrade();
        String alt2CuriNo = codingCertCriterion.getAlt2CuriNo();
        String alt2MinGrade = codingCertCriterion.getAlt2MinGrade();
        return isAltCourseCompleted(earnedCourses, alt1CuriNo, alt1MinGrade)
            || isAltCourseCompleted(earnedCourses, alt2CuriNo, alt2MinGrade);
    }

    private boolean isAltCourseCompleted(
        List<CompletedCourse> earnedCourses,
        String altCuriNo,
        String minGrade
    ) {
        GradeThreshold requirement = GradeThreshold.from(minGrade);
        if (requirement == null) {
            return false;
        }

        return earnedCourses.stream()
            .filter(completedCourse -> altCuriNo.equals(completedCourse.getCuriNo()))
            .anyMatch(completedCourse -> requirement.satisfiedMinGrade(completedCourse.getGrade()));
    }
}
