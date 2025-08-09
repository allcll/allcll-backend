package kr.allcll.backend.domain.subject.subjectReport;

import java.time.Duration;
import java.time.LocalDateTime;

public record CrawlingMetaData(
    LocalDateTime crawlingStartTime,
    LocalDateTime crawlingEndTime
) {

    public String formattedDuration() {
        Duration duration = crawlingDuration();
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%d분 %d초", minutes, seconds);
    }

    private Duration crawlingDuration() {
        return Duration.between(crawlingStartTime, crawlingEndTime);
    }

}
