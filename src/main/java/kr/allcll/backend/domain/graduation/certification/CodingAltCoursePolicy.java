package kr.allcll.backend.domain.graduation.certification;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
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
        List<CompletedCourseDto> completedCourses,
        GraduationCheckCertResult certResult
    ) {
        if (isAlreadyPassed(certResult)) {
            return false;
        }

        CodingTargetType codingTargetType = departmentInfo.getCodingTargetType();
        if (isExempt(codingTargetType)) {
            return false;
        }

        return codingCertCriterionRepository
            .findCodingCertCriterion(user.getAdmissionYear(), codingTargetType)
            .filter(codingCertCriterion -> isAltCourseCompletedByTargetType(
                    codingTargetType,
                    completedCourses,
                    codingCertCriterion
                )
            )
            .isPresent();
    }

    private boolean isAlreadyPassed(GraduationCheckCertResult certResult) {
        return Boolean.TRUE.equals(certResult.getIsCodingCertPassed());
    }

    private boolean isExempt(CodingTargetType codingTargetType) {
        return CodingTargetType.EXEMPT.equals(codingTargetType);
    }

    private boolean isAltCourseCompletedByTargetType(
        CodingTargetType codingTargetType,
        List<CompletedCourseDto> completedCourses,
        CodingCertCriterion codingCertCriterion
    ) {
        if (CodingTargetType.CODING_MAJOR.equals(codingTargetType)) {
            return isAltCourseCompletedForCodingMajor(completedCourses, codingCertCriterion);
        }
        if (CodingTargetType.NON_MAJOR.equals(codingTargetType)) {
            return isAltCourseCompletedForNonMajor(completedCourses, codingCertCriterion);
        }
        return false;
    }

    private boolean isAltCourseCompletedForCodingMajor(
        List<CompletedCourseDto> completedCourses,
        CodingCertCriterion codingCertCriterion
    ) {
        String alt1CuriNo = codingCertCriterion.getAlt1CuriNo();
        String alt1MinGrade = codingCertCriterion.getAlt1MinGrade();
        return isAltCourseCompleted(completedCourses, alt1CuriNo, alt1MinGrade);
    }

    private boolean isAltCourseCompletedForNonMajor(
        List<CompletedCourseDto> completedCourses,
        CodingCertCriterion codingCertCriterion
    ) {
        String alt1CuriNo = codingCertCriterion.getAlt1CuriNo();
        String alt1MinGrade = codingCertCriterion.getAlt1MinGrade();
        String alt2CuriNo = codingCertCriterion.getAlt2CuriNo();
        String alt2MinGrade = codingCertCriterion.getAlt2MinGrade();
        return isAltCourseCompleted(completedCourses, alt1CuriNo, alt1MinGrade)
            || isAltCourseCompleted(completedCourses, alt2CuriNo, alt2MinGrade);
    }

    private boolean isAltCourseCompleted(
        List<CompletedCourseDto> completedCourses,
        String altCuriNo,
        String minGrade
    ) {
        GradeThreshold requirement = GradeThreshold.from(minGrade);
        if (requirement == null) {
            return false;
        }

        return completedCourses.stream()
            .filter(completedCourse -> altCuriNo.equals(completedCourse.curiNo()))
            .anyMatch(completedCourse -> requirement.satisfiedMinGrade(completedCourse.grade()));
    }
}
