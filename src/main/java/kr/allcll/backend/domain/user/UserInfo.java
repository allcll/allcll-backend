package kr.allcll.backend.domain.user;

import lombok.Builder;

@Builder
public record UserInfo(
    String studentId,
    String name,
    String deptNm
) {

    public static UserInfo of(String studentId, String name, String dept_nm) {
        return UserInfo.builder()
            .studentId(studentId)
            .name(name)
            .deptNm(dept_nm)
            .build();
    }
}
