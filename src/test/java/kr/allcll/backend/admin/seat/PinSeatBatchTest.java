package kr.allcll.backend.admin.seat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kr.allcll.backend.fixture.CrawlerSeatFixture;
import kr.allcll.backend.fixture.CrawlerSubjectFixture;
import kr.allcll.crawler.seat.CrawlerSeat;
import kr.allcll.crawler.subject.CrawlerSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PinSeatBatchTest {

    private PinSeatBatch pinSeatBatch;

    @BeforeEach
    void setUp() {
        pinSeatBatch = new PinSeatBatch();
    }

    @AfterEach
    void flush() {
        pinSeatBatch.getAll();
    }

    @Test
    @DisplayName("인메모리 저장 후 핀 과목의 반환을 확인한다.")
    void savePinSeatToBatch() {
        // given
        CrawlerSubject crawlerSubjectA = CrawlerSubjectFixture.createCrawlerSubject("1234", "5678");
        CrawlerSeat crawlerSeatA = CrawlerSeatFixture.createCrawlerSeat(crawlerSubjectA);
        CrawlerSubject crawlerSubjectB = CrawlerSubjectFixture.createCrawlerSubject("5678", "1234");
        CrawlerSeat crawlerSeatB = CrawlerSeatFixture.createCrawlerSeat(crawlerSubjectB);

        // when
        pinSeatBatch.savePinSeatToBatch(crawlerSeatA);
        pinSeatBatch.savePinSeatToBatch(crawlerSeatB);
        List<CrawlerSeat> allCrawlerSeat = pinSeatBatch.getAll();

        // then
        assertThat(allCrawlerSeat).hasSize(2);
    }

    @Test
    @DisplayName("핀 과목을 반환할 경우 버퍼는 비워진다.")
    void batchEmptyAfterDrain() {
        // given
        CrawlerSubject crawlerSubjectA = CrawlerSubjectFixture.createCrawlerSubject("1234", "5678");
        CrawlerSeat crawlerSeatA = CrawlerSeatFixture.createCrawlerSeat(crawlerSubjectA);
        CrawlerSubject crawlerSubjectB = CrawlerSubjectFixture.createCrawlerSubject("5678", "1234");
        CrawlerSeat crawlerSeatB = CrawlerSeatFixture.createCrawlerSeat(crawlerSubjectB);

        // when
        pinSeatBatch.savePinSeatToBatch(crawlerSeatA);
        pinSeatBatch.savePinSeatToBatch(crawlerSeatB);
        pinSeatBatch.getAll();
        List<CrawlerSeat> afterDrain = pinSeatBatch.getAll();

        // then
        assertThat(afterDrain).isEmpty();
    }
}
