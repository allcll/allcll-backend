package kr.allcll.backend.basket.dto;

import java.util.List;

public record BasketsResponse(
    List<BasketsEachSubject> baskets
) {

}
