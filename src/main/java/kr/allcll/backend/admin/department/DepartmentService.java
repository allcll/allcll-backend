package kr.allcll.backend.admin.department;

import java.util.List;
import kr.allcll.backend.domain.department.Department;
import kr.allcll.backend.domain.department.DepartmentRepository;
import kr.allcll.backend.domain.department.dto.DepartmentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentsResponse getAllDepartment() {
        List<Department> departments = departmentRepository.findAll();
        return DepartmentsResponse.from(departments);
    }
}
