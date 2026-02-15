package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;

public class UserFixture {

    private static final String DEFAULT_NAME = "allcll";
    private static final String DEFAULT_STUDENT_ID = "00000000";

    public static User singleMajorUser(
        int admissionYear,
        GraduationDepartmentInfo primaryDeptInfo
    ) {
        return new User(
            DEFAULT_STUDENT_ID,
            DEFAULT_NAME,
            admissionYear,
            MajorType.SINGLE,
            primaryDeptInfo.getCollegeNm(),
            primaryDeptInfo.getDeptNm(),
            primaryDeptInfo.getDeptCd(),
            null,
            null,
            null
        );
    }

    public static User doubleMajorUser(
        int admissionYear,
        GraduationDepartmentInfo primaryDeptInfo,
        GraduationDepartmentInfo doubleDeptInfo
    ) {
        return new User(
            DEFAULT_STUDENT_ID,
            DEFAULT_NAME,
            admissionYear,
            MajorType.DOUBLE,
            primaryDeptInfo.getCollegeNm(),
            primaryDeptInfo.getDeptNm(),
            primaryDeptInfo.getDeptCd(),
            doubleDeptInfo.getCollegeNm(),
            doubleDeptInfo.getDeptNm(),
            doubleDeptInfo.getDeptCd()
        );
    }
}

