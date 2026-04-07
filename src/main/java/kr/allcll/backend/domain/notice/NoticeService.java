package kr.allcll.backend.domain.notice;

import java.util.List;
import kr.allcll.backend.domain.notice.dto.NoticesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticesResponse getAllNotice() {
        List<Notice> allNotices = noticeRepository.findAllOrderedByCreatedAt();
        return NoticesResponse.from(allNotices);
    }
}
