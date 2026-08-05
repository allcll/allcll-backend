package kr.allcll.backend.admin.seat;

import static kr.allcll.backend.fixture.CrawlerSeatFixture.createCrawlerSeatWithLimits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.crawler.seat.CrawlerSeat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SeatUtilsTest {

    @ParameterizedTest
    @MethodSource("seatLimitTypes")
    @DisplayName("여석 계산 기준별 남은 여석을 반환한다.")
    void getRemainSeat(SeatUtilsType seatUtilsType, int expectedRemainSeat) {
        // given
        CrawlerSeat crawlerSeat = createCrawlerSeatWithLimits(50, 40, 30, 20, 60, 10);

        // when
        Integer remainSeat = SeatUtils.getRemainSeat(crawlerSeat, seatUtilsType);

        // then
        assertThat(remainSeat).isEqualTo(expectedRemainSeat);
    }

    @Test
    @DisplayName("수강 인원이 정원보다 많으면 남은 여석은 0이다.")
    void getRemainSeatWhenRegisteredCountExceedsLimit() {
        // given
        CrawlerSeat crawlerSeat = createCrawlerSeatWithLimits(5, 5, 5, 5, 5, 6);

        // when
        Integer remainSeat = SeatUtils.getRemainSeat(crawlerSeat, SeatUtilsType.GRADE_4);

        // then
        assertThat(remainSeat).isZero();
    }

    @Test
    @DisplayName("지원하지 않는 여석 계산 기준이면 예외가 발생한다.")
    void getRemainSeatWithUnsupportedSeatUtilsType() {
        // given
        CrawlerSeat crawlerSeat = createCrawlerSeatWithLimits(50, 40, 30, 20, 60, 10);

        // when & then
        assertThatThrownBy(() -> SeatUtils.getRemainSeat(crawlerSeat, null))
            .isInstanceOf(AllcllException.class)
            .hasMessage("지원하지 않는 여석 계산 기준입니다.");
    }

    private static Stream<Arguments> seatLimitTypes() {
        return Stream.of(
            Arguments.of(SeatUtilsType.GRADE_4, 40),
            Arguments.of(SeatUtilsType.GRADE_3, 30),
            Arguments.of(SeatUtilsType.GRADE_2, 20),
            Arguments.of(SeatUtilsType.GRADE_1, 10),
            Arguments.of(SeatUtilsType.TOTAL, 50)
        );
    }
}
