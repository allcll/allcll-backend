package kr.allcll.backend.domain.seat.preseat;

import kr.allcll.backend.domain.seat.preseat.dto.PreSeatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PreSeatApi {

    private final PreSeatService preSeatService;

    @GetMapping("/api/pre-seat")
    public ResponseEntity<PreSeatsResponse> getAllPreSeats() {
        PreSeatsResponse response = preSeatService.getAllPreSeats();
        return ResponseEntity.ok(response);
    }
}
