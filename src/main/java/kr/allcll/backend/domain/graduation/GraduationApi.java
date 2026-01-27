package kr.allcll.backend.domain.graduation;

import jakarta.servlet.http.HttpSession;
import kr.allcll.backend.domain.user.LoginApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class GraduationApi {

    private final GraduationService graduationService;

    @GetMapping("/api/graduation")
    public ResponseEntity<GraduationResponse> check(HttpSession session) {
        Long userId = (Long) session.getAttribute(LoginApi.LOGIN_SESSION);
        GraduationResponse response = graduationService.getResult(userId);
        return ResponseEntity.ok(response);
    }
}
