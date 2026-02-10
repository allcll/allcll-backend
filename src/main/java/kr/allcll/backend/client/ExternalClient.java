package kr.allcll.backend.client;

import kr.allcll.backend.admin.seat.TargetSubjectService;
import kr.allcll.backend.admin.seat.dto.PinSubjectUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(ExternalProperties.class)
public class ExternalClient {

    // 외부 의존성
    private final TargetSubjectService targetSubjectService;

    public void sendPinSubjects(PinSubjectUpdateRequest request) {
        targetSubjectService.loadPinSubjects(request);
    }
}
