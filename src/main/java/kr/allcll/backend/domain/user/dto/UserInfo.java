package kr.allcll.backend.domain.user.dto;

import lombok.Builder;

@Builder
public record UserInfo(
    String studentId,
    String name,
    String deptNm
) {

    public static UserInfo of(String studentId, String name, String deptNm) {
        return new UserInfo(
            studentId,
            name,
            deptNm
        );
    }
}
