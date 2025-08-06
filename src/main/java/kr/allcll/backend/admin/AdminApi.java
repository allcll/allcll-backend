package kr.allcll.backend.admin;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.session.SessionService;
import kr.allcll.backend.session.dto.CredentialResponse;
import kr.allcll.backend.session.dto.SetCredentialRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminApi {

    private final AdminService adminService;
    private final AdminRequestValidator validator;
    private final SessionService sessionService;

    @PostMapping("/api/admin/scheduler/start")
    public ResponseEntity<Void> startScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        adminService.startScheduling();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/admin/scheduler/cancel")
    public ResponseEntity<Void> cancelScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        adminService.cancelScheduling();
        return ResponseEntity.ok().build();
    }

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

    @PostMapping("/api/admin/session/cancel")
    public ResponseEntity<Void> cancelSessionScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        sessionService.cancelSessionScheduling();
        return ResponseEntity.ok().build();
    }
}
