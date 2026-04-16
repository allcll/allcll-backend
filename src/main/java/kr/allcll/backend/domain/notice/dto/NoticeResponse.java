package kr.allcll.backend.domain.notice.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.operationperiod.OperationType;

public record NoticeResponse(
    long id,
    String title,
    String content,
    OperationType operationType,
    LocalDateTime createdAt
) {

    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.getOperationType(),
            notice.getCreatedAt()
        );
    }
}
