package kr.allcll.backend.domain.department.dto;

import kr.allcll.backend.domain.department.Department;

public record DepartmentResponse(
    String departmentName,
    String departmentCode
) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getDeptDegree(), department.getDeptCd());
    }
}
