package kr.allcll.backend.domain.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import java.util.List;
import kr.allcll.backend.domain.notice.dto.NoticesResponse;
import kr.allcll.backend.domain.operationperiod.OperationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private NoticeService noticeService;

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
        NoticesResponse response = noticeService.getAllNotice();

        // then
        assertAll(
            () -> assertThat(response.notices()).hasSize(2),
            () -> assertThat(response.notices().getFirst().title()).isEqualTo("첫 번째"),
            () -> assertThat(response.notices().get(1).content()).isEqualTo("두 번째 내용"),
            () -> assertThat(response.notices().get(1).operationType()).isEqualTo(OperationType.BASKETS)
        );
    }
}
