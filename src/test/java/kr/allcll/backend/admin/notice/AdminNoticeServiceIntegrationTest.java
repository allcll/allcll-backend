package kr.allcll.backend.admin.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import kr.allcll.backend.admin.notice.dto.CreateNoticeRequest;
import kr.allcll.backend.admin.notice.dto.CreateNoticeResponse;
import kr.allcll.backend.admin.notice.dto.AdminNoticesResponse;
import kr.allcll.backend.admin.notice.dto.UpdateNoticeRequest;
import kr.allcll.backend.admin.notice.dto.UpdateNoticeResponse;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.notice.NoticeRepository;
import kr.allcll.backend.domain.operationperiod.OperationType;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class AdminNoticeServiceIntegrationTest {

    @Autowired
    private AdminNoticeService adminNoticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    @AfterEach
    void cleanUp() {
        noticeRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("공지 생성 시 실제로 저장되고 응답이 반환된다")
    void createNewNotice() {
        // given
        CreateNoticeRequest request = new CreateNoticeRequest(
            "공지 제목",
            "공지 내용",
            OperationType.GRADUATION
        );

        // when
        CreateNoticeResponse response = adminNoticeService.createNewNotice(request);

        // then
        Notice savedNotice = noticeRepository.findActiveById(response.id()).orElseThrow();
        assertAll(
            () -> assertThat(response.id()).isPositive(),
            () -> assertThat(savedNotice.getTitle()).isEqualTo("공지 제목"),
            () -> assertThat(savedNotice.getContent()).isEqualTo("공지 내용"),
            () -> assertThat(savedNotice.getOperationType()).isEqualTo(OperationType.GRADUATION)
        );
    }

    @Test
    @DisplayName("공지 전체 조회 시 삭제되지 않은 공지만 생성일 최신순으로 반환한다")
    void getAllNotice() {
        // given
        Notice oldNotice = Notice.of("이전 공지", "이전 내용", OperationType.GRADUATION);
        Notice latestNotice = Notice.of("최신 공지", "최신 내용", OperationType.LIVE);
        Notice deletedNotice = Notice.of("삭제 공지", "삭제 내용", OperationType.PRESEAT);

        ReflectionTestUtils.setField(oldNotice, "createdAt", LocalDateTime.of(2026, 3, 28, 12, 0));
        ReflectionTestUtils.setField(latestNotice, "createdAt", LocalDateTime.of(2026, 3, 29, 12, 0));
        ReflectionTestUtils.setField(deletedNotice, "createdAt", LocalDateTime.of(2026, 3, 27, 12, 0));

        noticeRepository.save(oldNotice);
        noticeRepository.save(latestNotice);
        Notice savedDeletedNotice = noticeRepository.save(deletedNotice);
        adminNoticeService.deleteNotice(savedDeletedNotice.getId());

        // when
        AdminNoticesResponse response = adminNoticeService.getAllNotice();

        // then
        assertAll(
            () -> assertThat(response.notices()).hasSize(2),
            () -> assertThat(response.notices().getFirst().title()).isEqualTo("최신 공지"),
            () -> assertThat(response.notices().get(1).title()).isEqualTo("이전 공지")
        );
    }

    @Test
    @DisplayName("공지 수정 시 null이 아닌 필드만 실제로 변경된다")
    void updateNotice() {
        // given
        Notice notice = Notice.of("기존 제목", "기존 내용", OperationType.GRADUATION);
        LocalDateTime createdAt = LocalDateTime.of(2020, 4, 5, 10, 0);
        LocalDateTime initialUpdatedAt = LocalDateTime.of(2020, 4, 5, 10, 0);
        ReflectionTestUtils.setField(notice, "createdAt", createdAt);
        ReflectionTestUtils.setField(notice, "updatedAt", initialUpdatedAt);
        notice = noticeRepository.save(notice);
        UpdateNoticeRequest request = new UpdateNoticeRequest(
            "변경 제목",
            null,
            OperationType.LIVE
        );

        // when
        UpdateNoticeResponse response = adminNoticeService.updateNotice(notice.getId(), request);

        // then
        Notice updatedNotice = noticeRepository.findActiveById(notice.getId()).orElseThrow();
        assertAll(
            () -> assertThat(response.title()).isEqualTo("변경 제목"),
            () -> assertThat(updatedNotice.getTitle()).isEqualTo("변경 제목"),
            () -> assertThat(updatedNotice.getContent()).isEqualTo("기존 내용"),
            () -> assertThat(updatedNotice.getOperationType()).isEqualTo(OperationType.LIVE)
        );
    }

    @Test
    @DisplayName("공지 삭제 시 soft delete 처리되어 활성 조회에서 제외된다")
    void deleteNotice() {
        // given
        Notice notice = noticeRepository.save(
            Notice.of("공지 제목", "공지 내용", OperationType.GRADUATION)
        );

        // when
        adminNoticeService.deleteNotice(notice.getId());

        // then
        Notice deletedNotice = noticeRepository.findById(notice.getId()).orElseThrow();
        assertAll(
            () -> assertThat(deletedNotice.isDeleted()).isTrue(),
            () -> assertThat(noticeRepository.findActiveById(notice.getId())).isEmpty(),
            () -> assertThat(adminNoticeService.getAllNotice().notices()).isEmpty()
        );
    }

    @Test
    @DisplayName("존재하지 않는 공지를 수정하면 NOTICE_NOT_FOUND 예외를 던진다")
    void updateNoticeWhenNoticeNotFound() {
        // given
        Long id = 999L;
        UpdateNoticeRequest request = new UpdateNoticeRequest("변경 제목", "변경 내용", OperationType.GRADUATION);

        // when & then
        assertThatThrownBy(() -> adminNoticeService.updateNotice(id, request))
            .isInstanceOf(AllcllException.class)
            .hasMessage(String.format(AllcllErrorCode.NOTICE_NOT_FOUND.getMessage(), id));
    }
}
