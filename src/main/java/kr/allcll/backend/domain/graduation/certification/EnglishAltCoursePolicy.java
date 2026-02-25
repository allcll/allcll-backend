package kr.allcll.backend.domain.graduation.certification;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnglishAltCoursePolicy implements GraduationCertificationAltCoursePolicy{

    private final EnglishCertCriterionRepository englishCertCriterionRepository;

    @Override
    public boolean isSatisfiedByAltCourse(
        User user,
        GraduationDepartmentInfo departmentInfo,
        List<CompletedCourse> completedCourses
    ) {
        EnglishTargetType englishTargetType = departmentInfo.getEnglishTargetType();

        return englishCertCriterionRepository
            .findEnglishCertCriterion(user.getAdmissionYear(), englishTargetType)
            .map(EnglishCertCriterion::getAltCuriNo)
            .filter(altCuriNo -> isAltCourseCompleted(completedCourses, altCuriNo))
            .isPresent();
    }

    private boolean isAltCourseCompleted(List<CompletedCourse> completedCourses, String altCuriNo) {
        return completedCourses.stream()
            .map(CompletedCourse::getCuriNo)
            .anyMatch(altCuriNo::equals);
    }
}
