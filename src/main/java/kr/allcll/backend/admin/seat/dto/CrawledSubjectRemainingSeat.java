package kr.allcll.backend.admin.seat.dto;

import java.time.LocalDateTime;

public record CrawledSubjectRemainingSeat(
    Long subjectId,
    Integer remainSeat,
    LocalDateTime createdAt
) {

    public static CrawledSubjectRemainingSeat of(Long subjectId, Integer remainSeat, LocalDateTime createdAt) {
        return new CrawledSubjectRemainingSeat(subjectId, remainSeat, createdAt);
    }
}
