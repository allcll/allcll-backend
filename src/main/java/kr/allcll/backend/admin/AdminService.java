package kr.allcll.backend.admin;

import kr.allcll.backend.domain.seat.GeneralSeatSender;
import kr.allcll.backend.domain.seat.PinSeatSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final GeneralSeatSender generalSeatSender;
    private final PinSeatSender pinSeatSender;

    public void startScheduling() {
        generalSeatSender.send();
        pinSeatSender.send();
    }

    public void cancelScheduling() {
        generalSeatSender.cancel();
        pinSeatSender.cancel();
    }
}
