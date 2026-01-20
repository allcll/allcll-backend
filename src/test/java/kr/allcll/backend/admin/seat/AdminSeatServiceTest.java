package kr.allcll.backend.admin.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import kr.allcll.backend.admin.seat.dto.SeatStatusResponse;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.web.TokenProvider;
import kr.allcll.crawler.common.properties.SjptProperties;
import kr.allcll.crawler.common.schedule.CrawlerScheduledTaskHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class AdminSeatServiceTest {

    @Autowired
    private CrawlerScheduledTaskHandler seatScheduler;

    @Autowired
    private AdminSeatService adminSeatService;

    @Autowired
    private SjptProperties sjptProperties;

    @AfterEach
    void cancelAllSchedule() {
        seatScheduler.cancelAll();
    }

    @Test
    @DisplayName("userId가 하나이고, 크롤링이 성공하고 있을 시 정상 반환을 검증한다.")
    void getSeatCrawlerStatus() {
        // given
        String prefixId = "[21011138]";
        for (int i = 0; i < sjptProperties.getRequestPerSecondCount(); i++) {
            seatScheduler.scheduleAtFixedRate(
                prefixId + TokenProvider.create(),
                () -> {
                },
                Duration.ofSeconds(1)
            );
        }
        AtomicLong lastSuccess =
            (AtomicLong) ReflectionTestUtils.getField(adminSeatService, "lastSuccessCrawlingTime");
        lastSuccess.set(System.currentTimeMillis());

        // when
        SeatStatusResponse seatCrawlerStatus = adminSeatService.getSeatCrawlerStatus();

        // then
        SeatStatusResponse expected = SeatStatusResponse.of("21011138", true);
        assertThat(seatCrawlerStatus).
            usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    @DisplayName("userId가 한 개 초과일 경우 예외가 발생한다.")
    void duplicatedUserIdException() {
        // given
        String prefixIdA = "[21011138]";
        String prefixIdB = "[20010187]";
        for (int i = 0; i < sjptProperties.getRequestPerSecondCount(); i++) {
            seatScheduler.scheduleAtFixedRate(
                prefixIdA + TokenProvider.create(),
                () -> {
                },
                Duration.ofSeconds(1)
            );
            seatScheduler.scheduleAtFixedRate(
                prefixIdB + TokenProvider.create(),
                () -> {
                },
                Duration.ofSeconds(1)
            );
        }
        AtomicLong lastSuccess =
            (AtomicLong) ReflectionTestUtils.getField(adminSeatService, "lastSuccessCrawlingTime");
        lastSuccess.set(System.currentTimeMillis());

        // when & then
        assertThatThrownBy(() -> adminSeatService.getSeatCrawlerStatus())
            .isInstanceOf(AllcllException.class)
            .hasMessage(AllcllErrorCode.SEAT_CRAWLING_IN_MULTIPLE_ACCOUNTS.getMessage());
    }

    @Test
    @DisplayName("크롤링 성공 시간이 3초 초과일 경우 활성 상태는 false이다.")
    void exceedSuccessTime() {
        // given
        String prefixId = "[21011138]";
        for (int i = 0; i < sjptProperties.getRequestPerSecondCount(); i++) {
            seatScheduler.scheduleAtFixedRate(
                prefixId + TokenProvider.create(),
                () -> {
                },
                Duration.ofSeconds(1)
            );
        }
        AtomicLong lastSuccess =
            (AtomicLong) ReflectionTestUtils.getField(adminSeatService, "lastSuccessCrawlingTime");
        lastSuccess.set(System.currentTimeMillis() - 3001);

        // when
        SeatStatusResponse seatCrawlerStatus = adminSeatService.getSeatCrawlerStatus();

        // then
        SeatStatusResponse expected = SeatStatusResponse.of("21011138", false);
        assertThat(seatCrawlerStatus).
            usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    @DisplayName("크롤링 세팅 인증정보가 없는 경우의 반환을 확인한다.")
    void notExistUserId() {
        // given & when
        SeatStatusResponse seatCrawlerStatus = adminSeatService.getSeatCrawlerStatus();

        // then
        SeatStatusResponse expected = SeatStatusResponse.of(null, false);
        assertThat(seatCrawlerStatus).
            usingRecursiveComparison()
            .isEqualTo(expected);
    }
}
