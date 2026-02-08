package kr.allcll.backend.domain.graduation.certification;

import kr.allcll.backend.domain.graduation.certification.dto.GraduationCertCriteriaResponse;
import kr.allcll.backend.support.web.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GraduationCertificationApi {

    private final GraduationCertCriteriaService graduationCertCriteriaService;

    @GetMapping("/api/graduation/certifications/criteria")
    public ResponseEntity<GraduationCertCriteriaResponse> getGraduationCertCriteria(@Auth Long userId) {
        GraduationCertCriteriaResponse response = graduationCertCriteriaService.getGraduationCertCriteria(userId);
        return ResponseEntity.ok(response);
    }
}
