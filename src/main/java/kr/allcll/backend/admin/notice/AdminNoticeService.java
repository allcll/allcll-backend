package kr.allcll.backend.admin.notice;

import java.util.List;
import kr.allcll.backend.admin.notice.dto.CreateNoticeRequest;
import kr.allcll.backend.admin.notice.dto.CreateNoticeResponse;
import kr.allcll.backend.admin.notice.dto.NoticesResponse;
import kr.allcll.backend.admin.notice.dto.UpdateNoticeRequest;
import kr.allcll.backend.admin.notice.dto.UpdateNoticeResponse;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.notice.NoticeRepository;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
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

    @Transactional
    public UpdateNoticeResponse updateNotice(Long id, UpdateNoticeRequest updateNoticeRequest) {
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.NOTICE_NOT_FOUND, id));
        notice.update(
            updateNoticeRequest.title(),
            updateNoticeRequest.content(),
            updateNoticeRequest.operationType()
        );
        return UpdateNoticeResponse.from(notice);
    }
}
