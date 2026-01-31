package kr.allcll.backend.domain.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.domain.user.dto.LoginResult;
import kr.allcll.backend.domain.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class LoginApi {

    public static final String LOGIN_SESSION = "ALLCLL_LOGIN_SESSION";

    public final LoginFacade loginFacade;
    public final UserService userService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest,
        HttpServletRequest httpRequest) throws IOException {
        LoginResult result = loginFacade.login(loginRequest);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(LOGIN_SESSION, result.userId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/auth/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<UserResponse> check(HttpSession session) {
        Long userId = (Long) session.getAttribute(LoginApi.LOGIN_SESSION);
        UserResponse response = userService.getResult(userId);
        return ResponseEntity.ok(response);
    }
}
