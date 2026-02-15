package kr.allcll.backend.domain.graduation.certification.dto;

public record EnglishCertAltCourseResponse(
    String altCuriNo,
    String altCuriNm,
    Integer altCuriCredit
) {

    public static EnglishCertAltCourseResponse of(String altCuriNo, String altCuriNm, Integer altCuriCredit) {
        return new EnglishCertAltCourseResponse(altCuriNo, altCuriNm, altCuriCredit);
    }
}
