package kr.allcll.backend.domain.seat;

import static kr.allcll.backend.fixture.SubjectFixture.createNonMajorSubject;
import static kr.allcll.backend.fixture.SubjectFixture.createSubject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDateTime;
import java.util.List;
import kr.allcll.backend.domain.seat.dto.SeatDto;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.domain.subject.SubjectRepository;
import kr.allcll.crawler.seat.ChangeStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class SeatStorageTest {

    @Autowired
    private SeatStorage seatStorage;

    @Autowired
    private SubjectRepository subjectRepository;

    @AfterEach
    void tearDown() {
        seatStorage.clear();
        subjectRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동일한 과목의 여석을 저장하면 최근 여석으로 갱신된다.")
    void addDuplicateSeatTest() {
        // given
        Subject subject = createSubject("컴퓨터구조", "003278", "001", "유재석");
        subjectRepository.save(subject);

        // when
        SeatDto previousSeatDto = new SeatDto(subject, 10, LocalDateTime.now(), ChangeStatus.IN);
        seatStorage.add(previousSeatDto);
        SeatDto updatedSeatDto = new SeatDto(subject, 5, LocalDateTime.now(), ChangeStatus.UPDATE);
        seatStorage.add(updatedSeatDto);

        // then
        List<SeatDto> seats = seatStorage.getSeats(List.of(subject));
        SeatDto resultSeatDto = seats.getFirst();

        assertThat(resultSeatDto.getSeatCount()).isEqualTo(updatedSeatDto.getSeatCount());
    }

    /*
        - 비전공 과목만 조회한다.
        - limit 개수만큼 조회한다.
        - 여석이 0인 것은 조회 안된다.
        - 여석이 적은 것부터 조회한다.
     */
    @Test
    @DisplayName("비전공 과목 여석이 규칙에 맞게 반환된다.")
    void getGeneralSeatsTest() {
        // given
        Subject subject0 = createSubject("정보보호개론", "003278", "001", "유재석");
        Subject subject1 = createSubject("컴퓨터구조", "003278", "001", "유재석");
        Subject subject2 = createSubject("운영체제", "003279", "001", "노홍철");
        Subject subject3 = createSubject("자료구조", "003280", "001", "하하");
        Subject subject4 = createSubject("알고리즘", "003281", "001", "길");
        Subject subject5 = createNonMajorSubject("수요집현강좌", "003278", "002", "정형돈");
        Subject subject6 = createNonMajorSubject("서양고전강독1", "003279", "002", "나영석");
        Subject subject7 = createNonMajorSubject("동양고전강독1", "003280", "003", "박명수");
        Subject subject8 = createNonMajorSubject("채플4", "003281", "004", "전진");
        Subject subject9 = createNonMajorSubject("일반물리학", "003281", "004", "전진");
        Subject subject10 = createNonMajorSubject("인공지능컨텐츠", "003281", "004", "전진");
        subjectRepository.saveAll(
            List.of(subject0, subject1, subject2, subject3, subject4, subject5, subject6, subject7, subject8, subject9,
                subject10));
        SeatDto seatDto1 = new SeatDto(subject0, 8, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto2 = new SeatDto(subject1, 7, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto3 = new SeatDto(subject2, 6, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto4 = new SeatDto(subject3, 5, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto5 = new SeatDto(subject4, 4, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto6 = new SeatDto(subject5, 3, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto7 = new SeatDto(subject6, 2, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto8 = new SeatDto(subject7, 1, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto9 = new SeatDto(subject8, 0, LocalDateTime.now(), ChangeStatus.OUT);
        SeatDto seatDto10 = new SeatDto(subject9, 2, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto11 = new SeatDto(subject10, 1, LocalDateTime.now(), ChangeStatus.IN);
        seatStorage.addAll(
            List.of(seatDto1, seatDto2, seatDto3, seatDto4, seatDto5, seatDto6, seatDto7, seatDto8, seatDto9, seatDto10,
                seatDto11));

        // when
        int queryLimit = 5;
        List<SeatDto> seats = seatStorage.getGeneralSeats(queryLimit);

        // then
        assertThat(seats).hasSize(queryLimit)
            .extracting(SeatDto::getSubject, SeatDto::getSeatCount)
            .containsExactly(
                tuple(subject8, 0),
                tuple(subject7, 1),
                tuple(subject10, 1),
                tuple(subject6, 2),
                tuple(subject9, 2)
            );
    }

    @Test
    @DisplayName("요청한 과목들의 여석을 반환한다.")
    void getSeatsTest() {
        // given
        Subject subject0 = createSubject("정보보호개론", "003278", "001", "유재석");
        Subject subject1 = createSubject("컴퓨터구조", "003278", "001", "유재석");
        Subject subject2 = createSubject("운영체제", "003279", "001", "노홍철");
        Subject subject3 = createSubject("자료구조", "003280", "001", "하하");
        Subject subject4 = createSubject("알고리즘", "003281", "001", "길");
        Subject subject5 = createNonMajorSubject("수요집현강좌", "003278", "002", "정형돈");
        Subject subject6 = createNonMajorSubject("서양고전강독1", "003279", "002", "나영석");
        Subject subject7 = createNonMajorSubject("동양고전강독1", "003280", "003", "박명수");
        Subject subject8 = createNonMajorSubject("채플4", "003281", "004", "전진");
        Subject subject9 = createNonMajorSubject("일반물리학", "003281", "004", "전진");
        Subject subject10 = createNonMajorSubject("인공지능컨텐츠", "003281", "004", "전진");
        subjectRepository.saveAll(
            List.of(subject0, subject1, subject2, subject3, subject4, subject5, subject6, subject7, subject8, subject9,
                subject10));
        SeatDto seatDto1 = new SeatDto(subject0, 8, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto2 = new SeatDto(subject1, 7, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto3 = new SeatDto(subject2, 6, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto4 = new SeatDto(subject3, 5, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto5 = new SeatDto(subject4, 4, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto6 = new SeatDto(subject5, 3, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto7 = new SeatDto(subject6, 2, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto8 = new SeatDto(subject7, 1, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto9 = new SeatDto(subject8, 0, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto10 = new SeatDto(subject9, 2, LocalDateTime.now(), ChangeStatus.IN);
        SeatDto seatDto11 = new SeatDto(subject10, 1, LocalDateTime.now(), ChangeStatus.IN);
        seatStorage.addAll(
            List.of(seatDto1, seatDto2, seatDto3, seatDto4, seatDto5, seatDto6, seatDto7, seatDto8, seatDto9, seatDto10,
                seatDto11));

        // when
        List<SeatDto> seats = seatStorage.getSeats(List.of(subject0, subject5, subject7));

        // then
        assertThat(seats).hasSize(3)
            .extracting(SeatDto::getSubject, SeatDto::getSeatCount)
            .containsExactly(
                tuple(subject0, 8),
                tuple(subject5, 3),
                tuple(subject7, 1)
            );
    }
}
