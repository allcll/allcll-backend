package kr.allcll.backend.admin.notice.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.operationPeriod.OperationType;

public record AdminNoticeResponse(
    long id,
    String title,
    String content,
    OperationType operationType,
    LocalDateTime createdAt
) {

    public static AdminNoticeResponse from(Notice notice) {
        return new AdminNoticeResponse(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.getOperationType(),
            notice.getCreatedAt()
        );
    }
}
