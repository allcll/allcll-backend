package kr.allcll.backend.domain.basket.dto;

import java.util.List;
import kr.allcll.backend.domain.basket.Basket;
import kr.allcll.backend.domain.subject.Subject;

public record BasketsEachSubject(
    Long subjectId,
    Integer totalCount //총 인원
) {

    public static BasketsEachSubject from(Subject subject, List<Basket> baskets) {
        if (baskets.isEmpty()) {
            return new BasketsEachSubject(
                subject.getId(),
                0
            );
        }
        return new BasketsEachSubject(
            subject.getId(),
            baskets.getFirst().getTotRcnt()
        );
    }
}
