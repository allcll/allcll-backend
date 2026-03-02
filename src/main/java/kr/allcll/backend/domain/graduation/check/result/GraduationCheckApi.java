package kr.allcll.backend.domain.graduation.check.result;

import kr.allcll.backend.domain.graduation.check.result.dto.CompletedCoursesResponse;
import kr.allcll.backend.domain.graduation.check.result.dto.GraduationCheckResponse;
import kr.allcll.backend.support.web.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class GraduationCheckApi {

    private final GraduationCheckService graduationCheckService;

    @PostMapping("/api/graduation/check")
    public ResponseEntity<Void> checkGraduation(
        @RequestParam("file") MultipartFile gradeExcel,
        @Auth Long userId
    ) {
        graduationCheckService.checkGraduationRequirements(userId, gradeExcel);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/graduation/check")
    public ResponseEntity<GraduationCheckResponse> getLatestCheckResult(@Auth Long userId) {
        GraduationCheckResponse response = graduationCheckService.getCheckResult(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/graduation/courses")
    public ResponseEntity<CompletedCoursesResponse> getAllCompletedCourses(@Auth Long userId) {
        CompletedCoursesResponse response = graduationCheckService.getAllCompletedCourses(userId);
        return ResponseEntity.ok(response);
    }
}
