package kr.allcll.backend.admin.session.dto;

import java.time.LocalDateTime;

public record UserSessionStatusResponse(
    String userId,
    boolean isActive,
    LocalDateTime startTime
) {

}
