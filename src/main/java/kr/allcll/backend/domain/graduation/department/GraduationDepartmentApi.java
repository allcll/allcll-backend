package kr.allcll.backend.domain.graduation.department;

import kr.allcll.backend.domain.graduation.department.dto.GraduationDepartmentsResponse;
import kr.allcll.backend.support.web.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GraduationDepartmentApi {

    private final GraduationDepartmentService graduationDepartmentService;

    @GetMapping("/api/graduation/departments")
    public ResponseEntity<GraduationDepartmentsResponse> getAllDepartments(@Auth Long userId) {
        GraduationDepartmentsResponse graduationDepartmentsResponse = graduationDepartmentService.getAllDepartments(
            userId);
        return ResponseEntity.ok(graduationDepartmentsResponse);
    }
}
