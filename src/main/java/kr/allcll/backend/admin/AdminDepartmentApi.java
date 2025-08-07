package kr.allcll.backend.admin;

import kr.allcll.backend.domain.department.DepartmentService;
import kr.allcll.backend.domain.department.dto.DepartmentsResponse;
import kr.allcll.crawler.department.CrawlerDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminDepartmentApi {

    private final CrawlerDepartmentService crawlerDepartmentService;
    private final DepartmentService departmentService;

    @PostMapping("/api/admin/departments")
    public void fetchAndSaveDepartments(
        @RequestParam String userId,
        @RequestParam String year,
        @RequestParam String semesterCode
    ) {
        crawlerDepartmentService.fetchAndSaveDepartments(userId, year, semesterCode);
    }

    @GetMapping("/api/admin/departments")
    public ResponseEntity<DepartmentsResponse> getAllDepartments() {
        DepartmentsResponse response = departmentService.retrieveAllDepartment();
        return ResponseEntity.ok(response);
    }
}
