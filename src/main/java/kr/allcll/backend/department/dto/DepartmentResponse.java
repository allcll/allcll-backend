package kr.allcll.backend.department.dto;

import kr.allcll.backend.department.Department;

public record DepartmentResponse(
    String departmentName,
    String departmentCode
) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getDeptDegree(), department.getDeptCd());
    }
}
