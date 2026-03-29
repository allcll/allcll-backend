package kr.allcll.backend.admin.notice.dto;

import kr.allcll.backend.domain.operationPeriod.OperationType;

public record CreateNoticeRequest(
    String title,
    String content,
    OperationType operationType
) {

}
