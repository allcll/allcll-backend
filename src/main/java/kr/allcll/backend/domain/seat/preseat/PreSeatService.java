package kr.allcll.backend.domain.seat.preseat;

import java.util.List;
import kr.allcll.backend.domain.seat.preseat.dto.PreSeatsResponse;
import kr.allcll.backend.admin.preseat.AllPreSeatBuffer;
import kr.allcll.backend.admin.preseat.dto.PreSeatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreSeatService {

    private final AllPreSeatBuffer allPreSeatBuffer;

    public PreSeatsResponse getAllPreSeats() {
        List<PreSeatResponse> preSeats = allPreSeatBuffer.getAllAndFlush();
        return PreSeatsResponse.from(preSeats);
    }
}
