package kr.allcll.backend.admin.basket;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminBasketApi {

    private final AdminBasketService adminBasketService;

    @PostMapping("/api/admin/basket/fetch")
    public ResponseEntity<Void> getSubjects(@RequestParam String userId) {
        adminBasketService.fetchAndSaveBaskets(userId);
        return ResponseEntity.ok().build();
    }
}
