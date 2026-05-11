package kr.allcll.backend.admin.session;

import java.util.List;
import kr.allcll.backend.admin.session.dto.CredentialResponse;
import kr.allcll.backend.admin.session.dto.SessionStatusResponse;
import kr.allcll.backend.admin.session.dto.SetCredentialRequest;
import kr.allcll.backend.admin.session.dto.UserSessionsStatusResponse;
import kr.allcll.crawler.credential.Credentials;
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
    private final Credentials credentials;

    @PostMapping("/api/admin/session")
    public ResponseEntity<Void> setCredential(@RequestBody SetCredentialRequest setCredentialRequest) {
        sessionService.setCredential(setCredentialRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/session")
    public ResponseEntity<CredentialResponse> getCredential(@RequestParam("userId") String userId) {
        CredentialResponse credentialResponse = sessionService.getCredential(userId);
        return ResponseEntity.ok().body(credentialResponse);
    }

    @PostMapping("/api/admin/session-keep-alive")
    public ResponseEntity<Void> startSessionScheduling(@RequestParam("userId") String userId) {
        sessionService.startSession(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/session/check")
    public ResponseEntity<SessionStatusResponse> getSessionStatus(@RequestParam("userId") String userId) {
        SessionStatusResponse sessionStatusResponse = sessionService.getSessionStatus(userId);
        return ResponseEntity.ok().body(sessionStatusResponse);
    }

    @GetMapping("/api/admin/sessions/check")
    public ResponseEntity<UserSessionsStatusResponse> getSessionsStatus() {
        List<String> usersId = credentials.getAllUserIds();
        UserSessionsStatusResponse sessionsStatusResponse = sessionService.getSessionsStatus(usersId);
        return ResponseEntity.ok().body(sessionsStatusResponse);
    }

    @PostMapping("/api/admin/session/cancel")
    public ResponseEntity<Void> cancelSessionScheduling() {
        sessionService.cancelSessionScheduling();
        return ResponseEntity.ok().build();
    }
}
