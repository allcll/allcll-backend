package kr.allcll.backend.domain.graduation;

import kr.allcll.backend.domain.user.User;

public record UserSummary(
    Long id,
    String studentId,
    String name,
    String dept
) {

    public static UserSummary from(User user) {
        return new UserSummary(
            user.getId(),
            user.getStudentId(),
            user.getName(),
            user.getDeptNm()
        );
    }
}
