package kr.allcll.backend.admin.preseat;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.backend.admin.AdminRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminPreSeatApi {

    private final AdminPreSeatService adminPreSeatService;
    private final AdminRequestValidator validator;

    @PostMapping("/api/admin/pre-seat/fetch")
    public ResponseEntity<Void> getAllPreSeats(HttpServletRequest request,
        @RequestParam(required = false) String userId) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        adminPreSeatService.getAllPreSeat(userId);
        return ResponseEntity.ok().build();
    }
}
