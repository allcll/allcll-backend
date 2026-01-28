package kr.allcll.backend.domain.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.domain.user.dto.LoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class LoginApi {

    public static final String LOGIN_SESSION = "ALLCLL_LOGIN_SESSION";

    public final LoginFacade loginFacade;

    @PostMapping("/api/auth/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest,
        HttpServletRequest httpRequest) throws IOException {
        LoginResult result = loginFacade.login(loginRequest.studentId(), loginRequest.password());

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(LOGIN_SESSION, result.userId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/auth/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }
}
