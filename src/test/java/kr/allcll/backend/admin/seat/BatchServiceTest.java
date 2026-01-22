package kr.allcll.backend.admin.seat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class BatchServiceTest {

    @Autowired
    private BatchService batchService;

    @MockitoBean
    private PinSeatBatch pinSeatBatch;

    @MockitoBean
    private GeneralSeatBatch generalSeatBatch;

    @MockitoBean
    private SeatPersistenceService seatPersistenceService;

    @AfterEach
    void flush() {
        batchService.cancelFlushScheduling();
    }

    @Test
    @DisplayName("pin 일괄 저장이 3초에 3번 이루어진다.")
    void savePinSeatBatch() throws InterruptedException {
        // given
        int pinBatchCount = 3;
        when(pinSeatBatch.getAll()).thenReturn(List.of());

        // when
        batchService.flushPinSeatPeriodically();
        Thread.sleep(3000);
        batchService.cancelFlushScheduling();
        Thread.sleep(1000);

        // then
        verify(seatPersistenceService, atLeast(pinBatchCount)).saveAllSeat(any());
    }

    @Test
    @DisplayName("교양 과목 일괄 저장이 6초에 2번 이루어진다.")
    void saveGeneralSeatBatch() throws InterruptedException {
        // given
        int generalBatchCount = 2;
        when(generalSeatBatch.getAll()).thenReturn(List.of());

        // when
        batchService.flushGeneralSeatPeriodically();
        Thread.sleep(6000);
        batchService.cancelFlushScheduling();
        Thread.sleep(1000);

        // then
        verify(seatPersistenceService, atLeast(generalBatchCount)).saveAllSeat(any());
    }
}
