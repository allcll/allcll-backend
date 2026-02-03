package kr.allcll.backend.domain.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.domain.user.dto.LoginResult;
import kr.allcll.backend.domain.user.dto.UpdateUserRequest;
import kr.allcll.backend.domain.user.dto.UserResponse;
import kr.allcll.backend.support.web.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthApi {

    public static final String LOGIN_SESSION = "ALLCLL_LOGIN_SESSION";

    public final AuthFacade authFacade;
    public final UserService userService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest,
        HttpServletRequest httpRequest) {
        LoginResult result = authFacade.login(loginRequest);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(LOGIN_SESSION, result.userId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Cookie sessionCookie = new Cookie("JSESSIONID", "");
        sessionCookie.setMaxAge(0);
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<UserResponse> check(@Auth Long userId) {
        UserResponse response = userService.getResult(userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/auth/me")
    public ResponseEntity<Void> update(@Auth Long userId, @RequestBody UpdateUserRequest updateUserRequest) {
        userService.update(userId, updateUserRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/auth/me")
    public ResponseEntity<Void> delete(@Auth Long userId,
        HttpServletRequest request, HttpServletResponse response) {
        userService.delete(userId);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Cookie sessionCookie = new Cookie("JSESSIONID", "");
        sessionCookie.setMaxAge(0);
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);

        return ResponseEntity.noContent().build();
    }
}
