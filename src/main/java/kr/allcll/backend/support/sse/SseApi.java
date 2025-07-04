package kr.allcll.backend.support.sse;

import kr.allcll.backend.support.web.ThreadLocalHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseApi {

    private final SseService sseService;

    @GetMapping(value = "/api/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> getServerSentEventConnection() {
        String token = ThreadLocalHolder.SHARED_TOKEN.get();
        SseEmitter emitter = sseService.connect(token);
        return ResponseEntity.ok()
            .header("X-Accel-Buffering", "no")
            .body(emitter);
    }
}
