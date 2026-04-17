package kr.allcll.backend.admin.notice.dto;

import jakarta.validation.constraints.Size;
import kr.allcll.backend.domain.operationperiod.OperationType;

public record UpdateNoticeRequest(
    @Size(max = 250)
    String title,

    @Size(max = 1000)
    String content,

    OperationType operationType
) {

}
