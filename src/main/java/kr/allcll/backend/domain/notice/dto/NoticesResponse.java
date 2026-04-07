package kr.allcll.backend.domain.notice.dto;

import java.util.List;
import kr.allcll.backend.domain.notice.Notice;

public record NoticesResponse(
    List<NoticeResponse> notices
) {

    public static NoticesResponse from(List<Notice> allNotices) {
        return new NoticesResponse(
            allNotices.stream()
                .map(NoticeResponse::from)
                .toList()
        );
    }
}
