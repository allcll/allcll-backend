package kr.allcll.backend.admin.session;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import kr.allcll.backend.admin.session.dto.CredentialResponse;
import kr.allcll.backend.admin.session.dto.SessionStatusResponse;
import kr.allcll.backend.admin.session.dto.SetCredentialRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminSessionApi {

    private final SessionService sessionService;
    private final AdminRequestValidator validator;

    @PostMapping("/api/admin/session")
    public ResponseEntity<Void> setCredential(HttpServletRequest request,
        @RequestBody SetCredentialRequest setCredentialRequest) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        sessionService.setCredential(setCredentialRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/session")
    public ResponseEntity<CredentialResponse> getCredential(HttpServletRequest request,
        @RequestParam("userId") String userId) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        CredentialResponse credentialResponse = sessionService.getCredential(userId);
        return ResponseEntity.ok().body(credentialResponse);
    }

    @PostMapping("/api/admin/session-keep-alive")
    public ResponseEntity<Void> startSessionScheduling(HttpServletRequest request,
        @RequestParam("userId") String userId) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        sessionService.startSession(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/session/check")
    public ResponseEntity<SessionStatusResponse> getSessionStatus(HttpServletRequest request,
        @RequestParam("userId") String userId) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        SessionStatusResponse sessionStatusResponse = sessionService.getSessionStatus(userId);
        return ResponseEntity.ok().body(sessionStatusResponse);
    }

    @PostMapping("/api/admin/session/cancel")
    public ResponseEntity<Void> cancelSessionScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        sessionService.cancelSessionScheduling();
        return ResponseEntity.ok().build();
    }
}
