package kr.allcll.backend.admin.notice;

import java.util.List;
import kr.allcll.backend.admin.notice.dto.NoticesResponse;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.notice.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAnnouncementService {

    private final NoticeRepository noticeRepository;

    public NoticesResponse getAllNotice() {
        List<Notice> allNotices = noticeRepository.findAll();
        return NoticesResponse.from(allNotices);
    }
}
