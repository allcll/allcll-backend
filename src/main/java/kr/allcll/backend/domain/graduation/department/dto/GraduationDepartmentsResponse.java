package kr.allcll.backend.domain.graduation.department.dto;

import java.util.List;

public record GraduationDepartmentsResponse(
    Integer admissionYear,
    List<GraduationDepartmentResponse> departments
) {

    public static GraduationDepartmentsResponse of(Integer admissionYear, List<GraduationDepartmentResponse> departments) {
        return new GraduationDepartmentsResponse(admissionYear, departments);
    }
}
