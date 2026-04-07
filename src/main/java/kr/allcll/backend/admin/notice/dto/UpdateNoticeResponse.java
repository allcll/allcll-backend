package kr.allcll.backend.admin.notice.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.operationPeriod.OperationType;

public record UpdateNoticeResponse(
    long id,
    String title,
    String content,
    OperationType operationType,
    LocalDateTime updatedAt
) {

    public static UpdateNoticeResponse from(Notice notice) {
        return new UpdateNoticeResponse(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.getOperationType(),
            notice.getUpdatedAt()
        );
    }
}
