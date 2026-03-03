package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;

public class UserFixture {

    private static final String DEFAULT_NAME = "allcll";
    private static final String DEFAULT_STUDENT_ID = "00000000";

    public static User singleMajorUser(
        int admissionYear,
        GraduationDepartmentInfo primaryuserDept
    ) {
        return new User(
            DEFAULT_STUDENT_ID,
            DEFAULT_NAME,
            admissionYear,
            MajorType.SINGLE,
            primaryuserDept.getCollegeNm(),
            primaryuserDept.getDeptNm(),
            primaryuserDept.getDeptCd(),
            null,
            null,
            null
        );
    }

    public static User doubleMajorUser(
        int admissionYear,
        GraduationDepartmentInfo primaryuserDept,
        GraduationDepartmentInfo doubleuserDept
    ) {
        return new User(
            DEFAULT_STUDENT_ID,
            DEFAULT_NAME,
            admissionYear,
            MajorType.DOUBLE,
            primaryuserDept.getCollegeNm(),
            primaryuserDept.getDeptNm(),
            primaryuserDept.getDeptCd(),
            doubleuserDept.getCollegeNm(),
            doubleuserDept.getDeptNm(),
            doubleuserDept.getDeptCd()
        );
    }
}

