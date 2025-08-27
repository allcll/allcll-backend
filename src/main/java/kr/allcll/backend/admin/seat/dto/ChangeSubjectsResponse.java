package kr.allcll.backend.admin.seat.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.admin.seat.ChangeStatus;

public record ChangeSubjectsResponse(
    Long subjectId,
    ChangeStatus changeStatus,
    Integer remainSeat,
    LocalDateTime createdAt
) {

    // 변경 범위 최소화 하기 위한 정적 팩토리 메서드 - 여석 정책 변경 시 채택 필요
    public static ChangeSubjectsResponse of(Long subjectId, ChangeStatus changeStatus, Integer remainSeat) {
        return new ChangeSubjectsResponse(subjectId, changeStatus, remainSeat, LocalDateTime.now());
    }

    public static ChangeSubjectsResponse of(Long subjectId, ChangeStatus changeStatus, Integer remainSeat,
        LocalDateTime createdAt) {
        return new ChangeSubjectsResponse(subjectId, changeStatus, remainSeat, createdAt);
    }
}
