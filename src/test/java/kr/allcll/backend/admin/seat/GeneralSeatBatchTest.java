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

class GeneralSeatBatchTest {

    private GeneralSeatBatch generalSeatBatch;

    @BeforeEach
    void setUp() {
        generalSeatBatch = new GeneralSeatBatch();
    }

    @AfterEach
    void flush() {
        generalSeatBatch.getAll();
    }

    @Test
    @DisplayName("인메모리 저장 후 교양 좌석 반환을 확인한다.")
    void saveGeneralSeatToBatch() {
        // given
        CrawlerSubject crawlerSubjectA = CrawlerSubjectFixture.createCrawlerSubject("1234", "5678");
        CrawlerSeat crawlerSeatA = CrawlerSeatFixture.createCrawlerSeat(crawlerSubjectA);
        CrawlerSubject crawlerSubjectB = CrawlerSubjectFixture.createCrawlerSubject("5678", "1234");
        CrawlerSeat crawlerSeatB = CrawlerSeatFixture.createCrawlerSeat(crawlerSubjectB);

        // when
        generalSeatBatch.saveGeneralSeatToBatch(crawlerSeatA);
        generalSeatBatch.saveGeneralSeatToBatch(crawlerSeatB);
        List<CrawlerSeat> allSeat = generalSeatBatch.getAll();

        // then
        assertThat(allSeat).hasSize(2);
    }

    @Test
    @DisplayName("교양 좌석을 반환할 경우 버퍼는 비워진다.")
    void batchEmptyAfterDrain() {
        // given
        CrawlerSubject crawlerSubjectA = CrawlerSubjectFixture.createCrawlerSubject("1234", "5678");
        CrawlerSeat crawlerSeatA = CrawlerSeatFixture.createCrawlerSeat(crawlerSubjectA);
        CrawlerSubject crawlerSubjectB = CrawlerSubjectFixture.createCrawlerSubject("5678", "1234");
        CrawlerSeat crawlerSeatB = CrawlerSeatFixture.createCrawlerSeat(crawlerSubjectB);

        // when
        generalSeatBatch.saveGeneralSeatToBatch(crawlerSeatA);
        generalSeatBatch.saveGeneralSeatToBatch(crawlerSeatB);
        generalSeatBatch.getAll();
        List<CrawlerSeat> afterDrain = generalSeatBatch.getAll();

        // then
        assertThat(afterDrain).isEmpty();
    }
}
