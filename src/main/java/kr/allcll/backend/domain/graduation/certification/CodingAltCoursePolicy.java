package kr.allcll.backend.domain.graduation.certification;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodingAltCoursePolicy {

    private final CodingCertCriterionRepository codingCertCriterionRepository;

    public void applyIfSatisfied(
        User user,
        GraduationDepartmentInfo departmentInfo,
        List<CompletedCourseDto> completedCourses,
        GraduationCheckCertResult certResult
    ) {
        if (Boolean.TRUE.equals(certResult.getIsCodingCertPassed())) {
            return;
        }

        CodingTargetType codingTargetType = departmentInfo.getCodingTargetType();
        if (CodingTargetType.EXEMPT.equals(codingTargetType)) {
            return;
        }

        codingCertCriterionRepository
            .findCodingCertCriterionForTarget(user.getAdmissionYear(), codingTargetType)
            .filter(codingCertCriterion -> isAltCourseCompletedByTargetType(
                    codingTargetType,
                    completedCourses,
                    codingCertCriterion
                )
            )
            .ifPresent(codingCertCriterion -> certResult.updateCodingPassedByAltCourse());
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
