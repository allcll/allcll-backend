package kr.allcll.seatfinder.support.fixture;

import kr.allcll.seatfinder.basket.Basket;
import kr.allcll.seatfinder.subject.Subject;

public class BasketFixture {

    /*
        빈 Basket은 아래와 같이 생성됩니다.
        0과 null로 초기화된 필드는 수정하면 안됩니다.
        위 기준은 실제 API 요청으로 만들었습니다.
     */
    public static Basket createEmptyBasket(Subject subject) {
        return new Basket(subject, null, null, "", "", null, null,
            null, null, null, null, 99, 0, 0,
            0, 99, null, 0);
    }
}
