package kr.allcll.backend.domain.seat;

import static kr.allcll.backend.fixture.SubjectFixture.createMajorSubject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.seat.pin.Pin;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.support.semester.Semester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeatServiceUnitTest {

    @Mock
    private PinRepository pinRepository;

    @Mock
    private SeatStorage seatStorage;

    @InjectMocks
    private SeatService seatService;

    @Test
    @DisplayName("여러 토큰의 핀 과목 여석을 한 번의 핀 조회로 그룹핑한다.")
    void getPinSeatsByTokens() {
        // given
        String firstToken = "FIRST_TOKEN";
        String secondToken = "SECOND_TOKEN";
        String inactiveToken = "INACTIVE_TOKEN";
        Subject firstSubject = createMajorSubject(1L, "컴퓨터네트워크", "000001", "001", "유재석");
        Subject secondSubject = createMajorSubject(2L, "운영체제", "000002", "001", "유재석");
        Subject inactiveSubject = createMajorSubject(3L, "알고리즘", "000003", "001", "유재석");

        given(pinRepository.findAllBySemesterAt(Semester.getCurrentSemester()))
            .willReturn(List.of(
                new Pin(firstToken, firstSubject),
                new Pin(secondToken, secondSubject),
                new Pin(inactiveToken, inactiveSubject)
            ));

        LocalDateTime now = LocalDateTime.now();
        SeatDto firstSeat = new SeatDto(firstSubject, 1, now);
        SeatDto secondSeat = new SeatDto(secondSubject, 2, now);
        given(seatStorage.getSeats(List.of(firstSubject))).willReturn(List.of(firstSeat));
        given(seatStorage.getSeats(List.of(secondSubject))).willReturn(List.of(secondSeat));

        // when
        Map<String, List<SeatDto>> result = seatService.getPinSeatsByTokens(List.of(firstToken, secondToken));

        // then
        assertThat(result)
            .containsEntry(firstToken, List.of(firstSeat))
            .containsEntry(secondToken, List.of(secondSeat))
            .doesNotContainKey(inactiveToken);
        verify(pinRepository, times(1)).findAllBySemesterAt(Semester.getCurrentSemester());
        verify(pinRepository, never()).findAllByToken(anyString(), anyString());
        verify(seatStorage, times(2)).getSeats(any());
    }

    @Test
    @DisplayName("연결된 토큰이 없으면 핀 과목을 조회하지 않는다.")
    void getPinSeatsByTokensWithEmptyTokens() {
        // when
        Map<String, List<SeatDto>> result = seatService.getPinSeatsByTokens(List.of());

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(pinRepository);
        verifyNoInteractions(seatStorage);
    }
}
