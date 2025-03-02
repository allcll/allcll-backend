package kr.allcll.backend.config;

import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllSseException;
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

    public void validateSseConnection() {
        if (sseNotAccessible()) {
            throw new AllcllSseException(AllcllErrorCode.SSE_CONNECTION_DENIED);
        }
    }

    public void connectionOpen() {
        sseAccessible = true;
    }

    public void connectionClose() {
        sseAccessible = false;
    }
}
