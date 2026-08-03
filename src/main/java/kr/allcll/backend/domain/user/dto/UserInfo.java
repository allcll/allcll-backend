package kr.allcll.backend.domain.user.dto;

import lombok.Builder;

@Builder
public record UserInfo(
    String studentId,
    String name,
    String deptNm
) {

    private static final String FILM_ART_DEPARTMENT = "영화예술학과";
    private static final String PAINTING_DEPARTMENT = "회화과";
    private static final String MUSIC_DEPARTMENT = "음악과";
    private static final String PHYSICAL_EDUCATION_DEPARTMENT = "체육학과";
    private static final String DANCE_DEPARTMENT = "무용과";

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
        if (deptNm == null) {
            return null;
        }
        return switch (deptNm) {
            case "연기예술", "연출제작" -> FILM_ART_DEPARTMENT;
            case "서양화", "한국화" -> PAINTING_DEPARTMENT;
            case "성악", "피아노", "플루트", "클라리넷", "바이올린", "비올라", "첼로" ->
                MUSIC_DEPARTMENT;
            case "골프", "태권도", "축구", "리듬체조", "에어로빅체조", "사격", "수영", "양궁" ->
                PHYSICAL_EDUCATION_DEPARTMENT;
            case "발레", "한국무용", "현대무용" -> DANCE_DEPARTMENT;
            default -> deptNm;
        };
    }
}
