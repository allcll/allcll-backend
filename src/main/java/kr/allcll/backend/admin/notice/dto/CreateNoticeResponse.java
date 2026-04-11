package kr.allcll.backend.admin.notice.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.operationPeriod.OperationType;

public record CreateNoticeResponse(
    long id,
    String title,
    String content,
    OperationType operationType,
    LocalDateTime createdAt
) {

    public static CreateNoticeResponse from(Notice notice) {
        return new CreateNoticeResponse(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            notice.getOperationType(),
            notice.getCreatedAt()
        );
    }
}
