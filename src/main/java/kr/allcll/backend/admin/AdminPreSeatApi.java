package kr.allcll.backend.admin;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.crawler.seat.preseat.CrawlerPreSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminPreSeatApi {

    private final CrawlerPreSeatService crawlerPreSeatService;
    private final AdminRequestValidator validator;

    @PostMapping("/api/admin/pre-seat/fetch")
    public ResponseEntity<Void> getAllPreSeats(HttpServletRequest request,
        @RequestParam(required = false) String userId) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        crawlerPreSeatService.getAllPreSeat(userId);
        return ResponseEntity.ok().build();
    }
}
