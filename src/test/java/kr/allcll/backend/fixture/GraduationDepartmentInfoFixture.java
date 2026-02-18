package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.certification.CodingTargetType;
import kr.allcll.backend.domain.graduation.certification.EnglishTargetType;
import kr.allcll.backend.domain.graduation.department.DeptGroup;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;

public class GraduationDepartmentInfoFixture {

    public static GraduationDepartmentInfo createDepartmentInfo(int admissionYear, CodingTargetType codingTargetType) {
        return new GraduationDepartmentInfo(
            admissionYear,
            admissionYear % 100,
            "테스트학과",
            "3220",
            "테스트대학",
            DeptGroup.SOFTWARE_CONVERGENCE_COLLEGE,
            EnglishTargetType.NON_MAJOR,
            codingTargetType,
            null
        );
    }
}
