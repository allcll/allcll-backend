package kr.allcll.seatfinder.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.allcll.seatfinder.external.dto.NonMajorRequest;
import kr.allcll.seatfinder.external.dto.PinSubjectsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(ExternalProperties.class)
public class ExternalClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient restClient;
    private final ExternalProperties externalProperties;

    public void sendNonMajor(NonMajorRequest request) {
        String payload = toJson(request);
        restClient.put()
            .uri(externalProperties.host() + externalProperties.nonMajorPath())
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .toEntity(String.class);
    }

    public void sendPinSubjects(PinSubjectsRequest request) {
        String payload = toJson(request);
        restClient.put()
            .uri(externalProperties.host() + externalProperties.pinPath())
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .toEntity(String.class);
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 중 오류 발생", e);
        }
    }
}
