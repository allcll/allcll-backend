package kr.allcll.backend.domain.graduation;

import kr.allcll.backend.domain.user.User;

public record GraduationResponse(
    UserSummary userSummary
) {

    public static GraduationResponse from(User user) {
        return new GraduationResponse(UserSummary.from(user));
    }
}
