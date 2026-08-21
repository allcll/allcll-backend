package kr.allcll.backend.admin.seat;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import kr.allcll.backend.domain.seat.SeatStorage;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.crawler.subject.CrawlerSubject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TargetSubjectStalenessMetrics {

    private static final String TYPE_TAG = "type";
    private static final String PIN_TYPE = "pin";
    private static final String GENERAL_TYPE = "general";
    private static final long STALE_THRESHOLD_SECONDS = 60;
    private static final long COLLECT_INTERVAL_MILLIS = 15_000;

    private final TargetSubjectStorage targetSubjectStorage;
    private final SeatStorage seatStorage;
    private final Clock clock;
    private final Map<String, AtomicLong> stalenessMaxSeconds = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> staleCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> targetCounts = new ConcurrentHashMap<>();

    public TargetSubjectStalenessMetrics(
        TargetSubjectStorage targetSubjectStorage,
        SeatStorage seatStorage,
        Clock clock,
        MeterRegistry meterRegistry
    ) {
        this.targetSubjectStorage = targetSubjectStorage;
        this.seatStorage = seatStorage;
        this.clock = clock;
        for (String type : List.of(PIN_TYPE, GENERAL_TYPE)) {
            registerGauge(meterRegistry, "seat.target.staleness.max", type, stalenessMaxSeconds);
            registerGauge(meterRegistry, "seat.target.stale.count", type, staleCounts);
            registerGauge(meterRegistry, "seat.target.count", type, targetCounts);
        }
    }

    @Scheduled(fixedDelay = COLLECT_INTERVAL_MILLIS)
    void collect() {
        Map<Long, LocalDateTime> lastCrawledAt = seatStorage.getAll().stream()
            .collect(Collectors.toMap(
                seatDto -> seatDto.getSubject().getId(),
                SeatDto::getQueryTime,
                (first, second) -> first.isAfter(second) ? first : second
            ));
        record(PIN_TYPE, targetSubjectStorage.getTargetSubjects(), lastCrawledAt);
        record(GENERAL_TYPE, targetSubjectStorage.getTargetGeneralSubjects(), lastCrawledAt);
    }

    private void record(String type, List<CrawlerSubject> targets, Map<Long, LocalDateTime> lastCrawledAt) {
        LocalDateTime now = LocalDateTime.now(clock);
        long maxStaleness = 0;
        long staleCount = 0;
        for (CrawlerSubject target : targets) {
            LocalDateTime crawledAt = lastCrawledAt.get(target.getId());
            if (crawledAt == null) {
                // 크롤 대상인데 SeatStorage에 기록이 없으면 한 번도 크롤링되지 않은 기아 상태
                staleCount++;
                continue;
            }
            long stalenessSeconds = Duration.between(crawledAt, now).getSeconds();
            maxStaleness = Math.max(maxStaleness, stalenessSeconds);
            if (stalenessSeconds >= STALE_THRESHOLD_SECONDS) {
                staleCount++;
            }
        }
        stalenessMaxSeconds.get(type).set(maxStaleness);
        staleCounts.get(type).set(staleCount);
        targetCounts.get(type).set(targets.size());
    }

    private void registerGauge(
        MeterRegistry meterRegistry,
        String name,
        String type,
        Map<String, AtomicLong> values
    ) {
        AtomicLong value = values.computeIfAbsent(type, ignored -> new AtomicLong(0));
        Gauge.builder(name, value, AtomicLong::get)
            .tags(Tags.of(TYPE_TAG, type))
            .register(meterRegistry);
    }
}
