package kr.allcll.backend.domain.graduation.certification;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnglishAltCoursePolicy implements GraduationCertificationAltCoursePolicy{

    private final EnglishCertCriterionRepository englishCertCriterionRepository;

    @Override
    public void applyIfSatisfied(
        User user,
        GraduationDepartmentInfo departmentInfo,
        List<CompletedCourseDto> completedCourses,
        GraduationCheckCertResult certResult
    ) {
        if (Boolean.TRUE.equals(certResult.getIsEnglishCertPassed())) {
            return;
        }

        EnglishTargetType englishTargetType = departmentInfo.getEnglishTargetType();

        englishCertCriterionRepository
            .findEnglishCertCriterionForTarget(user.getAdmissionYear(), englishTargetType)
            .map(EnglishCertCriterion::getAltCuriNo)
            .filter(altCuriNo -> isAltCourseCompleted(completedCourses, altCuriNo))
            .ifPresent(altCuriNo -> certResult.updateEnglishPassedByAltCourse());
    }

    private boolean isAltCourseCompleted(List<CompletedCourseDto> completedCourses, String altCuriNo) {
        return completedCourses.stream()
            .filter(CompletedCourseDto::isCreditEarned)
            .map(CompletedCourseDto::curiNo)
            .anyMatch(altCuriNo::equals);
    }
}
