package kr.allcll.seatfinder.external;

import kr.allcll.seatfinder.external.dto.NonMajorRequest;
import kr.allcll.seatfinder.external.dto.PinSubjectsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(ExternalProperties.class)
public class ExternalClient {

    private final RestClient restClient;
    private final ExternalProperties externalProperties;

    public void sendNonMajor(NonMajorRequest request) {
        ResponseEntity<String> response = restClient.post()
            .uri(externalProperties.host() + externalProperties.nonMajorPath())
            .body(request)
            .retrieve()
            .toEntity(String.class);
        log.info("교양 top20 전달 완료: {}", response.getBody());
    }

    public void sendPinSubjects(PinSubjectsRequest request) {
        ResponseEntity<String> response = restClient.post()
            .uri(externalProperties.host() + externalProperties.pinPath())
            .body(request)
            .retrieve()
            .toEntity(String.class);
        log.info("핀 과목 전달 완료: {}", response.getBody());
    }
}
