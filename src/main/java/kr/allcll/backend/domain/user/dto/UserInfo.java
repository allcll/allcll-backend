package kr.allcll.backend.domain.user.dto;

import lombok.Builder;

@Builder
public record UserInfo(
    String studentId,
    String name,
    String deptNm
) {

    private static final String FILM_ART_DEPARTMENT = "영화예술학과";

    public UserInfo {
        deptNm = normalizeDeptNm(deptNm);
    }

    public static UserInfo of(String studentId, String name, String deptNm) {
        return new UserInfo(
            studentId,
            name,
            deptNm
        );
    }

    private static String normalizeDeptNm(String deptNm) {
        if ("연기예술".equals(deptNm) || "연출제작".equals(deptNm)) {
            return FILM_ART_DEPARTMENT;
        }
        return deptNm;
    }
}
