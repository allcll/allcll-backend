package kr.allcll.backend.domain.graduation.credit.dto;

public record RequiredCourseResponse(
    String curiNo,
    String curiNm
) {
    public static RequiredCourseResponse of(String curiNo, String curiNm) {
        return new RequiredCourseResponse(curiNo, curiNm);
    }
}
