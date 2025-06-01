package kr.allcll.backend.support.semester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class UtilServiceTest {

    @Autowired
    private UtilService utilService;

    @MockitoBean
    private Clock clock;

    @Test
    @DisplayName("학기 기간 내인 경우에 학기 정보를 조회한다.")
    void getSemesterTest() {
        // when
        Instant instant = Instant.parse("2025-02-01T00:00:00Z");
        ZoneId zoneId = ZoneId.systemDefault();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        SemesterResponse semesterResponse = utilService.getSemester();

        // then
        assertAll(
            () -> assertThat(semesterResponse.semester()).isEqualTo("2025-1"),
            () -> assertThat(semesterResponse.period().startDate()).isEqualTo(LocalDate.of(2025, 2, 1)),
            () -> assertThat(semesterResponse.period().endDate()).isEqualTo(LocalDate.of(2025, 3, 31))
        );
    }

    @Test
    @DisplayName("학기 기간 외인 경우에 다음 학기 정보를 조회한다.")
    void getNextSemesterTest() {
        // when
        Instant instant = Instant.parse("2025-05-01T00:00:00Z");
        ZoneId zoneId = ZoneId.systemDefault();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        SemesterResponse semesterResponse = utilService.getSemester();

        // then
        assertAll(
            () -> assertThat(semesterResponse.code()).isEqualTo("SUMMER_25"),
            () -> assertThat(semesterResponse.semester()).isEqualTo("2025-여름"),
            () -> assertThat(semesterResponse.period().startDate()).isEqualTo(LocalDate.of(2025, 6, 1)),
            () -> assertThat(semesterResponse.period().endDate()).isEqualTo(LocalDate.of(2025, 6, 30))
        );
    }
}
