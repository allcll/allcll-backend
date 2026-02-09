package kr.allcll.backend.domain.user.dto;

import kr.allcll.backend.domain.graduation.MajorType;
import kr.allcll.backend.domain.user.User;

public record UserResponse(
    Long id,
    String studentId,
    String studentName,
    String deptName,
    String deptCd,
    MajorType majorType
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getStudentId(),
            user.getName(),
            user.getDeptNm(),
            user.getDeptCd(),
            user.getMajorType()
        );
    }
}

