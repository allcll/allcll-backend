package kr.allcll.seatfinder.seat;

import kr.allcll.seatfinder.seat.dto.PreSeatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeatApi {

    private final SeatService seatService;

    @GetMapping("/api/preSeat")
    public ResponseEntity<PreSeatsResponse> getAllPreSeats() {
        PreSeatsResponse response = seatService.getAllPreSeats();
        return ResponseEntity.ok(response);
    }
}
