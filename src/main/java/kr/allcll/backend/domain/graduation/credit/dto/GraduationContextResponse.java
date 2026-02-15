package kr.allcll.backend.domain.graduation.credit.dto;

import kr.allcll.backend.domain.graduation.MajorType;

public record GraduationContextResponse(
    Integer admissionYear,
    MajorType majorType,
    String primaryDeptCd,
    String primaryDeptNm,
    String doubleDeptCd,
    String doubleDeptNm
) {

    public static GraduationContextResponse of(
        Integer admissionYear,
        MajorType majorType,
        String primaryDeptCd,
        String primaryDeptNm,
        String doubleDeptCd,
        String doubleDeptNm
    ) {
        return new GraduationContextResponse(admissionYear, majorType, primaryDeptCd, primaryDeptNm, doubleDeptCd, doubleDeptNm);
    }
}
