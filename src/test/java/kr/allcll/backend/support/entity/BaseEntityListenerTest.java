package kr.allcll.backend.support.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import kr.allcll.backend.domain.notice.Notice;
import kr.allcll.backend.domain.notice.NoticeRepository;
import kr.allcll.backend.domain.operationPeriod.OperationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class BaseEntityListenerTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void cleanUp() {
        noticeRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("엔티티 저장 시 생성일시와 수정일시를 초기화한다")
    void initializeTimestampsOnPersist() {
        // given
        Notice notice = Notice.of("공지 제목", "공지 내용", OperationType.GRADUATION);

        // when
        Notice savedNotice = noticeRepository.saveAndFlush(notice);

        // then
        assertAll(
            () -> assertThat(savedNotice.getCreatedAt()).isNotNull(),
            () -> assertThat(savedNotice.getUpdatedAt()).isNotNull(),
            () -> assertThat(savedNotice.getSemesterAt()).isNotBlank()
        );
    }

    @Test
    @DisplayName("엔티티 수정 시 수정일시를 최신 시각으로 갱신한다")
    void updateUpdatedAtOnUpdate() {
        // given
        Notice notice = Notice.of("공지 제목", "공지 내용", OperationType.GRADUATION);
        LocalDateTime initialUpdatedAt = LocalDateTime.of(2020, 4, 5, 10, 0);
        ReflectionTestUtils.setField(
            notice,
            "updatedAt",
            initialUpdatedAt
        );
        ReflectionTestUtils.setField(
            notice,
            "createdAt",
            LocalDateTime.of(2020, 4, 5, 10, 0)
        );

        Notice savedNotice = noticeRepository.saveAndFlush(notice);
        Notice noticeToUpdate = noticeRepository.findById(savedNotice.getId()).orElseThrow();
        noticeToUpdate.update("변경 제목", null, null);

        // when
        noticeRepository.saveAndFlush(noticeToUpdate);
        entityManager.clear();

        // then
        Notice updatedNotice = noticeRepository.findById(savedNotice.getId()).orElseThrow();
        assertThat(updatedNotice.getUpdatedAt()).isAfter(initialUpdatedAt);
    }
}
