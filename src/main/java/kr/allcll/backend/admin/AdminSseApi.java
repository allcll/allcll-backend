package kr.allcll.backend.admin;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.support.scheduler.SchedulerService;
import kr.allcll.backend.support.sse.SseService;
import kr.allcll.backend.support.web.ThreadLocalHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class AdminSseApi {

    private final SseService sseService;
    private final SchedulerService schedulerService;
    private final AdminRequestValidator validator;

    @GetMapping(value = "/api/admin/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> getServerSentEventConnection(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        String token = ThreadLocalHolder.SHARED_TOKEN.get();
        SseEmitter emitter = sseService.connect(token);
        return ResponseEntity.ok()
            .header("X-Accel-Buffering", "no")
            .body(emitter);
    }

    @PostMapping("/api/admin/scheduler/start")
    public ResponseEntity<Void> startScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        schedulerService.startScheduling();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/admin/scheduler/cancel")
    public ResponseEntity<Void> cancelScheduling(HttpServletRequest request) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        schedulerService.cancelScheduling();
        return ResponseEntity.ok().build();
    }
}
