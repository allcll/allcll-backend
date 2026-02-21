package kr.allcll.backend.domain.graduation.certification;

import java.util.List;
import kr.allcll.backend.domain.graduation.check.cert.GraduationCheckCertResult;
import kr.allcll.backend.domain.graduation.check.excel.CompletedCourseDto;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;

public interface GraduationCertificationPolicy {

    void applyIfSatisfied(
        User user,
        GraduationDepartmentInfo departmentInfo,
        List<CompletedCourseDto> completedCourses,
        GraduationCheckCertResult certResult
    );
}
