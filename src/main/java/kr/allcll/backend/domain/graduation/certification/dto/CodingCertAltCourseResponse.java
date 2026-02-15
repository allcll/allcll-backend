package kr.allcll.backend.domain.graduation.certification.dto;

public record CodingCertAltCourseResponse(
    String alt1CuriNo,
    String alt1CuriNm,
    String alt1MinGrade,
    String alt2CuriNo,
    String alt2CuriNm,
    String alt2MinGrade
) {

    public static CodingCertAltCourseResponse of(
        String alt1CuriNo,
        String alt1CurieNm,
        String alt1minGrade,
        String alt2CuriNo,
        String alt2CurieNm,
        String alt2minGrade
    ) {
        return new CodingCertAltCourseResponse(alt1CuriNo, alt1CurieNm, alt1minGrade, alt2CuriNo, alt2CurieNm, alt2minGrade);
    }
}
