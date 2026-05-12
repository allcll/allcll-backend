package kr.allcll.backend.admin.graduation.dto;

import kr.allcll.backend.domain.user.User;

public record GraduationUserResponse(
    Long id,
    String studentId,
    String name,
    Integer admissionYear,
    String majorType,
    String collegeName,
    String deptName,
    String deptCode,
    String doubleCollegeName,
    String doubleDeptName,
    String doubleDeptCode
) {

    public static GraduationUserResponse from(User user) {
        return new GraduationUserResponse(
            user.getId(),
            user.getStudentId(),
            user.getName(),
            user.getAdmissionYear(),
            user.getMajorType().name(),
            user.getCollegeNm(),
            user.getDeptNm(),
            user.getDeptCd(),
            user.getDoubleCollegeNm(),
            user.getDoubleDeptNm(),
            user.getDoubleDeptCd()
        );
    }
}
