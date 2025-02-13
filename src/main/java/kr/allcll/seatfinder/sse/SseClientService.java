package kr.allcll.seatfinder.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import kr.allcll.seatfinder.exception.AllcllErrorCode;
import kr.allcll.seatfinder.exception.AllcllException;
import kr.allcll.seatfinder.external.ExternalProperties;
import kr.allcll.seatfinder.seat.PinRemainSeat;
import kr.allcll.seatfinder.seat.dto.SeatDto;
import kr.allcll.seatfinder.seat.SeatStorage;
import kr.allcll.seatfinder.subject.Subject;
import kr.allcll.seatfinder.subject.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(ExternalProperties.class)
public class SseClientService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final SeatStorage seatStorage;
    private final SubjectRepository subjectRepository;
    private final ExternalProperties externalProperties;

    public void getSseData() {
        try {
            String host = externalProperties.host() + externalProperties.connectionPath() + "?userId=1";
            HttpURLConnection connection = getConnection(host);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    String dataPart = line.substring("data:".length()).trim();
                    if (dataPart.startsWith("{")) {
                        PinRemainSeat pinRemainSeat = null;
                        try {
                            pinRemainSeat = objectMapper.readValue(dataPart, PinRemainSeat.class);
                        } catch (Exception e) {
                            log.error("JSON 파싱 실패: {}", dataPart, e);
                        }
                        Subject subject = subjectRepository.findById(pinRemainSeat.subjectId())
                            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SUBJECT_NOT_FOUND));
                        seatStorage.add(new SeatDto(subject, pinRemainSeat.remainSeat(), LocalDateTime.now()));
                    } else {
                        log.debug("JSON 형식이 아닌 data 값: {}", dataPart);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException("SSE 연결 중 오류 발생", e);
        }
    }

    private HttpURLConnection getConnection(String host) throws Exception {
        URL url = new URI(host).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/event-stream");
        connection.setDoOutput(true);
        return connection;
    }
}
