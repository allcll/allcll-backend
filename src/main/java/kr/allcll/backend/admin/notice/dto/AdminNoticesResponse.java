package kr.allcll.backend.admin.notice.dto;

import java.util.List;
import kr.allcll.backend.domain.notice.Notice;

public record AdminNoticesResponse(
    List<AdminNoticeResponse> notices
) {

    public static AdminNoticesResponse from(List<Notice> allNotices) {
        return new AdminNoticesResponse(
            allNotices.stream()
                .map(AdminNoticeResponse::from)
                .toList()
        );
    }
}
