package kr.allcll.backend.domain.graduation.department.dto;

public record GraduationDepartmentResponse(
    String deptCd,
    String deptNm,
    String collegeNm,
    String deptGroup,
    String englishTargetType,
    String codingTargetType
) {

}
