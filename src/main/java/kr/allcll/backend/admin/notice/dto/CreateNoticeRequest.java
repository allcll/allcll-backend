package kr.allcll.backend.admin.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.operationPeriod.OperationType;

public record CreateNoticeRequest(
    @NotBlank
    @Size(max = 250)
    String title,

    @NotBlank
    @Size(max = 1000)
    String content,

    @NotNull
    OperationType operationType
) {

    public Notice toEntity() {
        return new Notice(
            title,
            content,
            operationType
        );
    }
}
