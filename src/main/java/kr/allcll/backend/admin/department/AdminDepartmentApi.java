package kr.allcll.backend.admin.department;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.domain.department.dto.DepartmentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminDepartmentApi {

    private final AdminDepartmentService adminDepartmentService;
    private final DepartmentService departmentService;
    private final AdminRequestValidator validator;

    @PostMapping("/api/admin/departments")
    public ResponseEntity<Void> fetchAndSaveDepartments(HttpServletRequest request,
        @RequestParam String userId,
        @RequestParam String year,
        @RequestParam String semesterCode
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        adminDepartmentService.fetchAndSaveDepartments(userId, year, semesterCode);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/departments")
    public ResponseEntity<DepartmentsResponse> getAllDepartments(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        DepartmentsResponse response = departmentService.getAllDepartment();
        return ResponseEntity.ok(response);
    }
}
