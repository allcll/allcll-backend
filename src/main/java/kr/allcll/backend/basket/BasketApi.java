package kr.allcll.backend.basket;

import kr.allcll.backend.basket.dto.BasketsResponse;
import kr.allcll.backend.basket.dto.SubjectBasketsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BasketApi {

    private final BasketService basketService;

    @GetMapping("/api/baskets")
    public ResponseEntity<BasketsResponse> getBasketsBySearch(
        @RequestParam(required = false) String departmentCode,
        @RequestParam(required = false) String professorName,
        @RequestParam(required = false) String subjectName
    ) {
        BasketsResponse response = basketService.findBasketsByCondition(
            departmentCode,
            professorName,
            subjectName
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/baskets/{subjectId}")
    public ResponseEntity<SubjectBasketsResponse> getEachSubjectBaskets(@PathVariable Long subjectId) {
        SubjectBasketsResponse response = basketService.getEachSubjectBaskets(subjectId);
        return ResponseEntity.ok(response);
    }
}
