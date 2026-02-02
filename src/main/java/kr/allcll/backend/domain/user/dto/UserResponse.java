package kr.allcll.backend.domain.user.dto;

import kr.allcll.backend.domain.user.User;

public record UserResponse(
    Long id,
    String studentId,
    String name,
    String collegeNm,
    String deptNm,
    String deptCd,
    int admissionYear
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getStudentId(),
            user.getName(),
            user.getCollegeNm(),
            user.getDeptNm(),
            user.getDeptCd(),
            user.getAdmissionYear()
        );
    }
}

