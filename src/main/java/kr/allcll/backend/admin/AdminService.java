package kr.allcll.backend.admin;

import kr.allcll.backend.config.AdminConfigStorage;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminConfigStorage adminConfigStorage;

    public void sseConnect() {
        if (adminConfigStorage.sseAccessible()) {
            throw new AllcllException(AllcllErrorCode.SSE_CONNECTION_ALREADY_OPEN);
        }
        adminConfigStorage.connectionOpen();
    }

}
