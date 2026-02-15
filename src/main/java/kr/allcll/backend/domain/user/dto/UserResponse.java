package kr.allcll.backend.domain.user.dto;

import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.user.User;

public record UserResponse(
    Long id,
    String studentId,
    String name,
    int admissionYear,
    MajorType majorType,
    String collegeName,
    String deptName,
    String deptCode,
    String doubleCollegeName,
    String doubleDeptName,
    String doubleDeptCode
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getStudentId(),
            user.getName(),
            user.getAdmissionYear(),
            user.getMajorType(),
            user.getCollegeNm(),
            user.getDeptNm(),
            user.getDeptCd(),
            user.getDoubleCollegeNm(),
            user.getDoubleDeptNm(),
            user.getDoubleDeptCd()
        );
    }
}

