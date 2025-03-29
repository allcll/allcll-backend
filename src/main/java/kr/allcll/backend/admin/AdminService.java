package kr.allcll.backend.admin;

import kr.allcll.backend.domain.seat.GeneralSeatSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final GeneralSeatSender generalSeatSender;

    public void startScheduling() {
        generalSeatSender.send();
    }

    public void cancelScheduling() {
        generalSeatSender.cancel();
    }
}
