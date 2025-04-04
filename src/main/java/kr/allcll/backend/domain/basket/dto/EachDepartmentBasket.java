package kr.allcll.backend.domain.basket.dto;

import java.util.List;
import kr.allcll.backend.domain.basket.Basket;

public record EachDepartmentBasket(
    String studentBelong,
    String registerDepartment,
    Integer eachCount
) {

    public static List<EachDepartmentBasket> from(List<Basket> baskets) {
        return baskets.stream()
            .map(basket -> new EachDepartmentBasket(
                basket.getStudentDivNm(),
                basket.getStudentDeptCdNm(),
                basket.getRcnt()))
            .toList();
    }
}
