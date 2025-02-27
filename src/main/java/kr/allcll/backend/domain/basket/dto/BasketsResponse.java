package kr.allcll.backend.domain.basket.dto;

import java.util.List;

public record BasketsResponse(
    List<BasketsEachSubject> baskets
) {

}
