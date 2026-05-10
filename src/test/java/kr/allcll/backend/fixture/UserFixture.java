package kr.allcll.backend.fixture;

import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.graduation.department.GraduationDepartmentInfo;
import kr.allcll.backend.domain.user.User;

public class UserFixture {

    private static final String DEFAULT_NAME = "allcll";
    private static final String DEFAULT_STUDENT_ID = "00000000";
    private static final int DEFAULT_ADMISSION_YEAR = 2023;
    private static final String DEFAULT_COLLEGE = "테스트대학";
    private static final String DEFAULT_DEPT_NM = "테스트학과";
    private static final String DEFAULT_DEPT_CD = "3220";

    public static User singleMajorUser(String studentId) {
        return new User(
            studentId,
            DEFAULT_NAME,
            DEFAULT_ADMISSION_YEAR,
            MajorType.SINGLE,
            DEFAULT_COLLEGE,
            DEFAULT_DEPT_NM,
            DEFAULT_DEPT_CD,
            null,
            null,
            null
        );
    }

    public static User singleMajorUser(
        int admissionYear,
        GraduationDepartmentInfo primaryuserDept
    ) {
        return singleMajorUser(DEFAULT_STUDENT_ID, admissionYear, primaryuserDept);
    }

    public static User singleMajorUser(
        String studentId,
        int admissionYear,
        GraduationDepartmentInfo primaryuserDept
    ) {
        return new User(
            studentId,
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

