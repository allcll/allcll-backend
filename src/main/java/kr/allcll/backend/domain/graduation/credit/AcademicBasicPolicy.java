package kr.allcll.backend.domain.graduation.credit;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AcademicBasicPolicy {

    private final RequiredCourseResolver requiredCourseResolver;
    private final CourseReplacementRepository courseReplacementRepository;

    public boolean isRecentMajorAcademicBasic(CompletedCourse course, CreditCriterion criterion) {
        if (isNotAcademicBasic(course)) {
            return true;
        }
        String courseName = course.getCuriNm();
        Integer admissionYear = criterion.getAdmissionYear();
        String departmentName = criterion.getDeptNm();
        List<String> academicBasicRequiredCourseNames = requiredCourseResolver.findRequiredCourseNames(
            departmentName,
            admissionYear,
            CategoryType.ACADEMIC_BASIC
        );

        if (isExistAcademicBasicCourse(academicBasicRequiredCourseNames, courseName)) {
            return true;
        }

        return isHaveReplaceCourse(academicBasicRequiredCourseNames, admissionYear, courseName);
    }

    private boolean isExistAcademicBasicCourse(List<String> academicBasicRequiredCourseNames, String courseName) {
        return academicBasicRequiredCourseNames.contains(courseName);
    }

    /*
    대체된 최신 과목을 들었을 경우를 판별한다.
    대체된 최신 과목이 없는 경우 false를 반환한다.
    대체 과목의 예전 과목 명이, 학생의 이수 요건에 없으면 false를 반환한다.
     */
    private boolean isHaveReplaceCourse(
        List<String> academicBasicRequiredCourseNames,
        Integer admissionYear,
        String courseName
    ) {
        List<CourseReplacement> recentCourse = courseReplacementRepository.findRecentCourse(admissionYear, courseName);
        if (recentCourse.isEmpty()) {
            return false;
        }
        for (CourseReplacement courseReplacement : recentCourse) {
            if (academicBasicRequiredCourseNames.contains(courseReplacement.getLegacyCuriNm())) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotAcademicBasic(CompletedCourse course) {
        return !isAcademicBasic(course);
    }

    private boolean isAcademicBasic(CompletedCourse course) {
        return CategoryType.ACADEMIC_BASIC.equals(course.getCategoryType());
    }
}
