package kr.allcll.backend.admin.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminNoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private AdminNoticeService adminNoticeService;

    @Test
    @DisplayName("공지 전체 조회 시 NoticeResponse 목록으로 변환한다")
    void getAllNotice() {
        // given
        Notice firstNotice = Notice.of("첫 번째", "첫 번째 내용", OperationType.GRADUATION);
        Notice secondNotice = Notice.of("두 번째", "두 번째 내용", OperationType.BASKETS);
        ReflectionTestUtils.setField(firstNotice, "id", 1L);
        ReflectionTestUtils.setField(secondNotice, "id", 2L);
        given(noticeRepository.findAllOrderedByCreatedAt())
            .willReturn(List.of(firstNotice, secondNotice));

        // when
        AdminNoticesResponse response = adminNoticeService.getAllNotice();

        // then
        assertAll(
            () -> assertThat(response.notices()).hasSize(2),
            () -> assertThat(response.notices().getFirst().title()).isEqualTo("첫 번째"),
            () -> assertThat(response.notices().get(1).content()).isEqualTo("두 번째 내용"),
            () -> assertThat(response.notices().get(1).operationType()).isEqualTo(OperationType.BASKETS)
        );
    }

    @Test
    @DisplayName("공지 생성 시 저장 후 응답을 반환한다")
    void createNewNotice() {
        // given
        CreateNoticeRequest request = new CreateNoticeRequest(
            "공지 제목",
            "공지 내용",
            OperationType.GRADUATION
        );
        given(noticeRepository.save(any(Notice.class)))
            .willAnswer(invocation -> {
                Notice savedNotice = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedNotice, "id", 1L);
                return savedNotice;
            });

        // when
        CreateNoticeResponse response = adminNoticeService.createNewNotice(request);

        // then
        assertAll(
            () -> assertThat(response.title()).isEqualTo("공지 제목"),
            () -> assertThat(response.content()).isEqualTo("공지 내용"),
            () -> assertThat(response.operationType()).isEqualTo(OperationType.GRADUATION)
        );
        then(noticeRepository).should().save(any(Notice.class));
    }

    @Test
    @DisplayName("공지 수정 시 요청에 포함된 값만 변경한다")
    void updateNotice() {
        // given
        Long id = 1L;
        Notice notice = Notice.of("기존 제목", "기존 내용", OperationType.GRADUATION);
        ReflectionTestUtils.setField(notice, "id", id);
        UpdateNoticeRequest request = new UpdateNoticeRequest(
            "변경 제목",
            null,
            OperationType.BASKETS
        );
        given(noticeRepository.findActiveById(id))
            .willReturn(Optional.of(notice));

        // when
        UpdateNoticeResponse response = adminNoticeService.updateNotice(id, request);

        // then
        assertAll(
            () -> assertThat(response.title()).isEqualTo("변경 제목"),
            () -> assertThat(response.content()).isEqualTo("기존 내용"),
            () -> assertThat(response.operationType()).isEqualTo(OperationType.BASKETS)
        );
    }

    @Test
    @DisplayName("수정할 공지가 없으면 NOTICE_NOT_FOUND 예외를 던진다")
    void updateNoticeWhenNoticeNotFound() {
        // given
        Long id = 999L;
        UpdateNoticeRequest request = new UpdateNoticeRequest("변경 제목", "변경 내용", OperationType.GRADUATION);
        given(noticeRepository.findActiveById(id))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminNoticeService.updateNotice(id, request))
            .isInstanceOf(AllcllException.class)
            .hasMessage(String.format(AllcllErrorCode.NOTICE_NOT_FOUND.getMessage(), id));
    }

    @Test
    @DisplayName("공지 삭제 시 soft delete 처리한다")
    void deleteNotice() {
        // given
        Long id = 1L;
        Notice notice = Notice.of("공지 제목", "공지 내용", OperationType.GRADUATION);
        ReflectionTestUtils.setField(notice, "id", id);
        given(noticeRepository.findActiveById(id))
            .willReturn(Optional.of(notice));

        // when
        adminNoticeService.deleteNotice(id);

        // then
        assertThat(notice.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("삭제할 공지가 없으면 NOTICE_NOT_FOUND 예외를 던진다")
    void deleteNoticeWhenNoticeNotFound() {
        // given
        Long id = 999L;
        given(noticeRepository.findActiveById(id))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminNoticeService.deleteNotice(id))
            .isInstanceOf(AllcllException.class)
            .hasMessage(String.format(AllcllErrorCode.NOTICE_NOT_FOUND.getMessage(), id));
    }
}
