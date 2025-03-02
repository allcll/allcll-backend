package kr.allcll.backend.domain.util;

import java.time.LocalDate;
import kr.allcll.backend.domain.util.dto.SemesterResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class UtilServiceTest {

    @Autowired
    private UtilService utilService;

    @Test
    @DisplayName("현재 학기 정보를 조회한다.")
    void getSemesterTest() {
        // when
        SemesterResponse semesterResponse = utilService.getSemester();

        // then
        assertAll(
            () -> assertThat(semesterResponse.semester()).isEqualTo("2025-1"),
            () -> assertThat(semesterResponse.period().startDate()).isEqualTo(LocalDate.of(2025, 2, 1)),
            () -> assertThat(semesterResponse.period().endDate()).isEqualTo(LocalDate.of(2025, 3, 31))
        );
    }
}
