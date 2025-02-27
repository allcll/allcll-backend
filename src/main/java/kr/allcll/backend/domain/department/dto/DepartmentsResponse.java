package kr.allcll.backend.domain.department.dto;

import java.util.List;
import kr.allcll.backend.domain.department.Department;

public record DepartmentsResponse(
    List<DepartmentResponse> departments
) {

    public static DepartmentsResponse from(List<Department> departments) {
        List<DepartmentResponse> result = departments.stream()
            .map(DepartmentResponse::from)
            .toList();
        return new DepartmentsResponse(result);
    }
}
