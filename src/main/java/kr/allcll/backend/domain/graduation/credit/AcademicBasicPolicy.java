package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AcademicBasicPolicy {

    private final RequiredCourseResolver requiredCourseResolver;
    private final CourseEquivalenceRepository courseEquivalenceRepository;

    public boolean isRecentMajorAcademicBasic(CompletedCourse course, CreditCriterion criterion) {
        if (isNotAcademicBasic(course)) {
            return true;
        }
        String curiNm = course.getCuriNm();
        String curiNo = course.getCuriNo();
        Integer admissionYear = criterion.getAdmissionYear();
        String departmentName = criterion.getDeptNm();
        List<String> academicBasicRequiredCourseNames = requiredCourseResolver.findRequiredCourseNames(
            departmentName,
            admissionYear,
            CategoryType.ACADEMIC_BASIC
        );

        if (isExistAcademicBasicCourse(academicBasicRequiredCourseNames, curiNm)) {
            return true;
        }

        return isHaveReplaceOrEquivalenceCourse(admissionYear, departmentName, curiNo);
    }

    private boolean isExistAcademicBasicCourse(List<String> academicBasicRequiredCourseNames, String courseName) {
        return academicBasicRequiredCourseNames.contains(courseName);
    }

    private boolean isNotAcademicBasic(CompletedCourse course) {
        return !CategoryType.ACADEMIC_BASIC.equals(course.getCategoryType());
    }

    private boolean isHaveReplaceOrEquivalenceCourse(
        Integer admissionYear,
        String departmentName,
        String curiNo
    ) {
        return courseEquivalenceRepository.findGroupCodeByCuriNo(curiNo)
            .map(groupCode -> requiredCourseResolver.findRequiredCourseInGroup(
                departmentName,
                admissionYear,
                CategoryType.ACADEMIC_BASIC,
                groupCode
            ))
            .orElse(false);
    }
}
