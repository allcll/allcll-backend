package kr.allcll.seatfinder.seat;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;
import kr.allcll.seatfinder.subject.Subject;
import kr.allcll.seatfinder.support.fixture.SubjectFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.scheduling.enabled=true") // 테스트에서 스케줄러 활성화
class SeatServiceTest {

    private static final Logger log = LoggerFactory.getLogger(SeatServiceTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private SeatStorage seatStorage;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("1초에 한 번씩 비전공 과목의 좌석 정보 10개를 전송한다.")
    @Test
    void sendNonMajorSeatInfoTest() throws JsonProcessingException {
        // given
        String expected = """
            {
              "seatResponses": [
                {
                  "subjectId": 11,
                  "seatCount": 10,
                  "queryTime": "2025-1-28T23:55:23.434294"
                },
                {
                  "subjectId": 12,
                  "seatCount": 9,
                  "queryTime": "2025-1-28T23:55:23.434294"
                },
                {
                  "subjectId": 13,
                  "seatCount": 8,
                  "queryTime": "2025-1-28T23:55:23.434294"
                },
                {
                  "subjectId": 14,
                  "seatCount": 7,
                  "queryTime": "2025-1-28T23:55:23.434294"
                },
                {
                  "subjectId": 15,
                  "seatCount": 6,
                  "queryTime": "2025-1-28T23:55:23.434294"
                },
                {
                  "subjectId": 16,
                  "seatCount": 5,
                  "queryTime": "2025-1-28T23:55:23.434294"
                },
                {
                  "subjectId": 17,
                  "seatCount": 4,
                  "queryTime": "2025-1-28T23:55:23.434294"
                },
                {
                  "subjectId": 18,
                  "seatCount": 3,
                  "queryTime": "2025-1-28T23:55:23.434294"
                },
                {
                  "subjectId": 19,
                  "seatCount": 2,
                  "queryTime": "2025-1-28T23:55:23.434294"
                },
                {
                  "subjectId": 20,
                  "seatCount": 1,
                  "queryTime": "2025-1-28T23:55:23.434294"
                }
              ]
            }
            """;

        Subject majorSubject1 = SubjectFixture.createMajorSubject(1L, "컴퓨터네트워크", "000001", "001", "유재석");
        Subject majorSubject2 = SubjectFixture.createMajorSubject(2L, "컴퓨터네트워크", "000001", "002", "유재석");
        Subject majorSubject3 = SubjectFixture.createMajorSubject(3L, "컴퓨터네트워크", "000001", "003", "유재석");
        Subject majorSubject4 = SubjectFixture.createMajorSubject(4L, "컴퓨터네트워크", "000001", "004", "유재석");
        Subject majorSubject5 = SubjectFixture.createMajorSubject(5L, "컴퓨터네트워크", "000001", "005", "유재석");
        Subject nonMajorSubject1 = SubjectFixture.createNonMajorSubject(11L, "차와문화", "000002", "001", "정형돈");
        Subject nonMajorSubject2 = SubjectFixture.createNonMajorSubject(12L, "차와문화", "000002", "002", "정형돈");
        Subject nonMajorSubject3 = SubjectFixture.createNonMajorSubject(13L, "차와문화", "000002", "003", "정형돈");
        Subject nonMajorSubject4 = SubjectFixture.createNonMajorSubject(14L, "차와문화", "000002", "004", "정형돈");
        Subject nonMajorSubject5 = SubjectFixture.createNonMajorSubject(15L, "차와문화", "000002", "005", "정형돈");
        Subject nonMajorSubject6 = SubjectFixture.createNonMajorSubject(16L, "차와문화", "000002", "006", "정형돈");
        Subject nonMajorSubject7 = SubjectFixture.createNonMajorSubject(17L, "차와문화", "000002", "007", "정형돈");
        Subject nonMajorSubject8 = SubjectFixture.createNonMajorSubject(18L, "차와문화", "000002", "008", "정형돈");
        Subject nonMajorSubject9 = SubjectFixture.createNonMajorSubject(19L, "차와문화", "000002", "009", "정형돈");
        Subject nonMajorSubject10 = SubjectFixture.createNonMajorSubject(20L, "차와문화", "000002", "010", "정형돈");
        LocalDateTime localDateTime = LocalDateTime.of(2025, 1, 28, 23, 55, 23, 434294000);

        seatStorage.addAll(List.of(
            new Seat(majorSubject1, 20, localDateTime),
            new Seat(majorSubject2, 19, localDateTime),
            new Seat(majorSubject3, 18, localDateTime),
            new Seat(majorSubject4, 17, localDateTime),
            new Seat(majorSubject5, 16, localDateTime),
            new Seat(nonMajorSubject1, 10, localDateTime),
            new Seat(nonMajorSubject2, 9, localDateTime),
            new Seat(nonMajorSubject3, 8, localDateTime),
            new Seat(nonMajorSubject4, 7, localDateTime),
            new Seat(nonMajorSubject5, 6, localDateTime),
            new Seat(nonMajorSubject6, 5, localDateTime),
            new Seat(nonMajorSubject7, 4, localDateTime),
            new Seat(nonMajorSubject8, 3, localDateTime),
            new Seat(nonMajorSubject9, 2, localDateTime),
            new Seat(nonMajorSubject10, 1, localDateTime)
        ));

        // when
        Response response = RestAssured.given()
            .accept("text/event-stream")
            .when()
            .get("/api/connect")
            .then()
            .statusCode(200)
            .extract()
            .response();

        // then
        TestHelper.assertResponseContainsMessage(response, objectMapper.readTree(expected).asText());
    }

    private static class TestHelper {

        private static final long TIMEOUT = 1000;

        public static void assertResponseIsEqualsToMessage(Response response, String message) {
            String eventReceived = readResponse(response);
            assertThat(eventReceived).isEqualTo(message);
        }

        public static void assertResponseContainsMessage(Response response, String... messages) {
            String eventReceived = readResponse(response);
            assertThat(eventReceived).contains(messages);
        }

        private static String readResponse(Response response) {
            String body = response.getBody().asString();
            try (BufferedReader reader = new BufferedReader(new StringReader(body))) {
                return readLines(reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static String readLines(BufferedReader reader) throws IOException {
            StringBuilder lines = new StringBuilder();
            String line;
            long startTime = System.currentTimeMillis();
            while ((line = reader.readLine()) != null) {
                log.info("Received: {}", line);
                lines.append(line).append("\n");
                if (System.currentTimeMillis() - startTime > TIMEOUT) {
                    break;
                }
            }
            return lines.toString();
        }
    }

}
