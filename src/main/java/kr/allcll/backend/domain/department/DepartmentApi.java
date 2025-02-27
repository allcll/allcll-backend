package kr.allcll.backend.domain.department;

import kr.allcll.backend.domain.department.dto.DepartmentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DepartmentApi {

    private final DepartmentService departmentService;

    @GetMapping("/api/departments")
    public ResponseEntity<DepartmentsResponse> retrieveAllDepartment() {
        DepartmentsResponse response = departmentService.retrieveAllDepartment();
        return ResponseEntity.ok(response);
    }
}
