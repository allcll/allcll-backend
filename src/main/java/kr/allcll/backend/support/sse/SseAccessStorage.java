package kr.allcll.backend.support.sse;

import org.springframework.stereotype.Component;

@Component
public class SseAccessStorage {

    private boolean accessible;

    public SseAccessStorage() {
        accessible = false;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public boolean isNotAccessible() {
        return !isAccessible();
    }

    public void connectionOpen() {
        accessible = true;
    }

    public void connectionClose() {
        accessible = false;
    }
}
