package kr.allcll.backend.admin.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import kr.allcll.backend.domain.seat.SeatStorage;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.crawler.subject.CrawlerSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TargetSubjectStalenessMetricsTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-08-20T03:00:00Z"), ZONE_ID);
    private final LocalDateTime now = LocalDateTime.now(fixedClock);

    private SimpleMeterRegistry meterRegistry;
    private TargetSubjectStorage targetSubjectStorage;
    private SeatStorage seatStorage;
    private TargetSubjectStalenessMetrics targetSubjectStalenessMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        targetSubjectStorage = mock(TargetSubjectStorage.class);
        seatStorage = mock(SeatStorage.class);
        targetSubjectStalenessMetrics = new TargetSubjectStalenessMetrics(
            targetSubjectStorage, seatStorage, fixedClock, meterRegistry
        );
    }

    @Test
    @DisplayName("크롤 대상별 최대 staleness, 기준 초과 과목 수, 대상 수를 기록한다.")
    void recordStalenessMetricsForTargets() {
        // given: 핀 대상 3개 - 10초 전 크롤, 120초 전 크롤, 크롤 기록 없음
        CrawlerSubject fresh = crawlerSubject(1L);
        CrawlerSubject stale = crawlerSubject(2L);
        CrawlerSubject neverCrawled = crawlerSubject(3L);
        List<SeatDto> seatDtos = List.of(
            seatDto(1L, now.minusSeconds(10)),
            seatDto(2L, now.minusSeconds(120))
        );
        when(targetSubjectStorage.getTargetSubjects()).thenReturn(List.of(fresh, stale, neverCrawled));
        when(targetSubjectStorage.getTargetGeneralSubjects()).thenReturn(List.of());
        when(seatStorage.getAll()).thenReturn(seatDtos);

        // when
        targetSubjectStalenessMetrics.collect();

        // then
        assertThat(gaugeValue("seat.target.staleness.max", "pin")).isEqualTo(120.0);
        assertThat(gaugeValue("seat.target.stale.count", "pin")).isEqualTo(2.0);
        assertThat(gaugeValue("seat.target.count", "pin")).isEqualTo(3.0);
    }

    @Test
    @DisplayName("대상이 없으면 모든 지표가 0이다.")
    void recordZeroWhenNoTargets() {
        // given
        when(targetSubjectStorage.getTargetSubjects()).thenReturn(List.of());
        when(targetSubjectStorage.getTargetGeneralSubjects()).thenReturn(List.of());
        when(seatStorage.getAll()).thenReturn(List.of());

        // when
        targetSubjectStalenessMetrics.collect();

        // then
        assertThat(gaugeValue("seat.target.staleness.max", "general")).isZero();
        assertThat(gaugeValue("seat.target.stale.count", "general")).isZero();
        assertThat(gaugeValue("seat.target.count", "general")).isZero();
    }

    @Test
    @DisplayName("같은 과목의 크롤 기록이 여러 건이면 가장 최근 시각으로 staleness 를 계산한다.")
    void useLatestQueryTimeWhenDuplicated() {
        // given
        CrawlerSubject target = crawlerSubject(1L);
        List<SeatDto> seatDtos = List.of(
            seatDto(1L, now.minusSeconds(300)),
            seatDto(1L, now.minusSeconds(30))
        );
        when(targetSubjectStorage.getTargetSubjects()).thenReturn(List.of(target));
        when(targetSubjectStorage.getTargetGeneralSubjects()).thenReturn(List.of());
        when(seatStorage.getAll()).thenReturn(seatDtos);

        // when
        targetSubjectStalenessMetrics.collect();

        // then
        assertThat(gaugeValue("seat.target.staleness.max", "pin")).isEqualTo(30.0);
        assertThat(gaugeValue("seat.target.stale.count", "pin")).isZero();
    }

    private CrawlerSubject crawlerSubject(Long id) {
        CrawlerSubject crawlerSubject = mock(CrawlerSubject.class);
        when(crawlerSubject.getId()).thenReturn(id);
        return crawlerSubject;
    }

    private SeatDto seatDto(Long subjectId, LocalDateTime queryTime) {
        Subject subject = mock(Subject.class);
        when(subject.getId()).thenReturn(subjectId);
        return new SeatDto(subject, 1, queryTime);
    }

    private double gaugeValue(String name, String type) {
        return meterRegistry.get(name).tag("type", type).gauge().value();
    }
}
