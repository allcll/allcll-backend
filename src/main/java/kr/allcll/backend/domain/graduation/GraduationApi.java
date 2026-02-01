package kr.allcll.backend.domain.graduation;

import kr.allcll.backend.support.web.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GraduationApi {

    private final GraduationService graduationService;

    @GetMapping("/api/graduation")
    public ResponseEntity<GraduationResponse> check(@Auth Long userId) {
        GraduationResponse response = graduationService.getResult(userId);
        return ResponseEntity.ok(response);
    }
}
