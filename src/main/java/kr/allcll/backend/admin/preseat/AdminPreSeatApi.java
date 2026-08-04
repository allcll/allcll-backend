package kr.allcll.backend.admin.preseat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminPreSeatApi {

    private final AdminPreSeatService adminPreSeatService;

    @PostMapping("/api/admin/pre-seat/fetch")
    public ResponseEntity<Void> getAllPreSeats(@RequestParam(required = false) String userId) {
        adminPreSeatService.getAllPreSeat(userId);
        return ResponseEntity.ok().build();
    }
}
