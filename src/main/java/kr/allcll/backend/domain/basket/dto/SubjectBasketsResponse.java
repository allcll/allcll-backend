package kr.allcll.backend.domain.basket.dto;

import java.util.List;
import kr.allcll.backend.domain.basket.Basket;

public record SubjectBasketsResponse(
    Long everytimeLectureId,
    List<EachDepartmentBasket> eachDepartmentRegisters
) {

    public static SubjectBasketsResponse from(Long everytimeLectureId, List<Basket> baskets) {
        List<EachDepartmentBasket> result = EachDepartmentBasket.from(baskets);
        return new SubjectBasketsResponse(everytimeLectureId, result);
    }
}
