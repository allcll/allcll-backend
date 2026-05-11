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
        GraduationDepartmentInfo primaryUserDept
    ) {
        return singleMajorUser(DEFAULT_STUDENT_ID, admissionYear, primaryUserDept);
    }

    public static User singleMajorUser(
        String studentId,
        int admissionYear,
        GraduationDepartmentInfo primaryUserDept
    ) {
        return new User(
            studentId,
            DEFAULT_NAME,
            admissionYear,
            MajorType.SINGLE,
            primaryUserDept.getCollegeNm(),
            primaryUserDept.getDeptNm(),
            primaryUserDept.getDeptCd(),
            null,
            null,
            null
        );
    }

    public static User doubleMajorUser(
        int admissionYear,
        GraduationDepartmentInfo primaryUserDept,
        GraduationDepartmentInfo doubleUserDept
    ) {
        return new User(
            DEFAULT_STUDENT_ID,
            DEFAULT_NAME,
            admissionYear,
            MajorType.DOUBLE,
            primaryUserDept.getCollegeNm(),
            primaryUserDept.getDeptNm(),
            primaryUserDept.getDeptCd(),
            doubleUserDept.getCollegeNm(),
            doubleUserDept.getDeptNm(),
            doubleUserDept.getDeptCd()
        );
    }
}

