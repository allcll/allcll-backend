package kr.allcll.backend.admin;

import kr.allcll.backend.admin.dto.SystemStatusResponse;
import kr.allcll.backend.config.AdminConfigStorage;
import kr.allcll.backend.domain.seat.SeatService;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.schedule.ScheduleStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminConfigStorage adminConfigStorage;
    private final ScheduleStorage scheduleStorage;
    private final SeatService seatService;

    public void sseConnect() {
        if (adminConfigStorage.sseAccessible()) {
            throw new AllcllException(AllcllErrorCode.SSE_CONNECTION_ALREADY_OPEN);
        }
        adminConfigStorage.connectionOpen();
    }

    public void sseDisconnect() {
        if (adminConfigStorage.sseNotAccessible()) {
            throw new AllcllException(AllcllErrorCode.SSE_CONNECTION_ALREADY_CLOSED);
        }
        adminConfigStorage.connectionClose();
    }

    public void startToSendNonMajor() {
        if (adminConfigStorage.sseNotAccessible()) {
            throw new AllcllException(AllcllErrorCode.SSE_CONNECTION_DENIED);
        }
        seatService.sendNonMajorSeats();
    }

    public SystemStatusResponse getInitialStatus() {
        if (adminConfigStorage.sseNotAccessible() && scheduleStorage.isNonMajorScheduleNotRunning()) {
            return SystemStatusResponse.of(false, false);
        }
        if (adminConfigStorage.sseAccessible() && scheduleStorage.isNonMajorScheduleRunning()) {
            return SystemStatusResponse.of(true, true);
        }
        if (adminConfigStorage.sseAccessible() && scheduleStorage.isNonMajorScheduleNotRunning()) {
            return SystemStatusResponse.of(true, false);
        }
        throw new AllcllException(AllcllErrorCode.DATA_STREAM_SHOULD_SHUT_DOWN);
    }
}
