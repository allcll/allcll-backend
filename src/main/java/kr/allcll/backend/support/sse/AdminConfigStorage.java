package kr.allcll.backend.support.sse;

import org.springframework.stereotype.Component;

@Component
public class AdminConfigStorage {

    private boolean sseAccessible;

    public AdminConfigStorage() {
        sseAccessible = false;
    }

    public boolean sseAccessible() {
        return sseAccessible;
    }

    public boolean sseNotAccessible() {
        return !sseAccessible();
    }

    public void connectionOpen() {
        sseAccessible = true;
    }

    public void connectionClose() {
        sseAccessible = false;
    }
}
