package kr.allcll.backend.admin.department;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminDepartmentApi {

    private final AdminDepartmentService adminDepartmentService;

    @PostMapping("/api/admin/departments")
    public ResponseEntity<Void> fetchAndSaveDepartments(
        @RequestParam String userId,
        @RequestParam String year,
        @RequestParam String semesterCode
    ) {
        adminDepartmentService.fetchAndSaveDepartments(userId, year, semesterCode);
        return ResponseEntity.ok().build();
    }
}
