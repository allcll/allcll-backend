package kr.allcll.backend.admin.notice.dto;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.operationPeriod.OperationType;

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
