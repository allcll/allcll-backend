package kr.allcll.backend.domain.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.time.LocalDateTime;
import java.util.List;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.seat.pin.Pin;
import kr.allcll.backend.domain.seat.pin.PinRepository;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.backend.fixture.SubjectFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class SeatServiceTest {

    @Autowired
    private SeatService seatService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private SeatStorage seatStorage;
    @Autowired
    private PinSeatSender pinSeatSender;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        seatStorage.clear();
        pinRepository.deleteAll();
        subjectRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("핀 등록한 과목의 여석을 조회한다.")
    void getPinSeatsTest() {
        // given
        String token = "token";

        Subject subject1 = SubjectFixture.createSubject("컴퓨터네트워크", "000001", "001", "유재석");
        Subject subject2 = SubjectFixture.createSubject("컴퓨터네트워크", "000001", "002", "유재석");
        Subject subject3 = SubjectFixture.createSubject("컴퓨터네트워크", "000001", "003", "유재석");
        Subject subject4 = SubjectFixture.createSubject("컴퓨터네트워크", "000001", "004", "유재석");
        Subject subject5 = SubjectFixture.createSubject("컴퓨터네트워크", "000001", "005", "유재석");
        subjectRepository.saveAll(List.of(subject1, subject2, subject3, subject4, subject5));

        Pin pin1 = new Pin(token, subject1);
        Pin pin2 = new Pin(token, subject2);
        Pin pin3 = new Pin(token, subject3);
        Pin pin4 = new Pin(token, subject4);
        Pin pin5 = new Pin(token, subject5);
        pinRepository.saveAll(List.of(pin1, pin2, pin3, pin4, pin5));

        LocalDateTime now = LocalDateTime.now();
        SeatDto seat1 = new SeatDto(subject1, 1, now);
        SeatDto seat2 = new SeatDto(subject2, 2, now);
        SeatDto seat3 = new SeatDto(subject3, 3, now);
        SeatDto seat4 = new SeatDto(subject4, 4, now);
        SeatDto seat5 = new SeatDto(subject5, 5, now);
        seatStorage.addAll(List.of(seat1, seat2, seat3, seat4, seat5));

        // when
        List<SeatDto> pinSeats = seatService.getPinSeats(token);

        // then
        assertThat(pinSeats).hasSize(5)
            .extracting(
                pinSeat -> pinSeat.getSubject().getId(),
                SeatDto::getSeatCount,
                SeatDto::getQueryTime
            )
            .containsExactly(
                tuple(1L, 1, now),
                tuple(2L, 2, now),
                tuple(3L, 3, now),
                tuple(4L, 4, now),
                tuple(5L, 5, now)
            );
    }
}
