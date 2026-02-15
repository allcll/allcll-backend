package kr.allcll.backend.domain.graduation.check.result;

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
    public ResponseEntity<GraduationCheckResponse> checkGraduation(
        @RequestParam("file") MultipartFile gradeExcel,
        @Auth Long userId
    ) {
        GraduationCheckResponse response = graduationCheckService.checkGraduationRequirements(userId, gradeExcel);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/graduation/check")
    public ResponseEntity<GraduationCheckResponse> getLatestCheckResult(@Auth Long userId) {
        GraduationCheckResponse response = graduationCheckService.getCheckResult(userId);
        return ResponseEntity.ok(response);
    }
}
