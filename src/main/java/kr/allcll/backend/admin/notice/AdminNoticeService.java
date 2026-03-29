package kr.allcll.backend.admin.notice;

import java.util.List;
import kr.allcll.backend.admin.notice.dto.CreateNoticeRequest;
import kr.allcll.backend.admin.notice.dto.CreateNoticeResponse;
import kr.allcll.backend.admin.notice.dto.NoticesResponse;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.notice.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;

    public NoticesResponse getAllNotice() {
        List<Notice> allNotices = noticeRepository.findAllOrderedByCreatedAt();
        return NoticesResponse.from(allNotices);
    }

    @Transactional
    public CreateNoticeResponse createNewNotice(CreateNoticeRequest createNoticeRequest) {
        Notice notice = noticeRepository.save(createNoticeRequest.toEntity());
        return CreateNoticeResponse.from(notice);
    }
}
