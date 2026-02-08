package kr.allcll.backend.domain.graduation.certification.dto;

public record EnglishCertAltCourseResponse(
    String altCuriNo,
    String altCurieNm,
    Integer altCuricredit
) {

    public static EnglishCertAltCourseResponse of(String altCuriNo, String altCurieNm, Integer altCuricredit) {
        return new EnglishCertAltCourseResponse(altCuriNo, altCurieNm, altCuricredit);
    }
}
