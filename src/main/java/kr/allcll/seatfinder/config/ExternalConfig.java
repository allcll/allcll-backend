package kr.allcll.seatfinder.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExternalConfig {

    private final ExternalPreInvoker externalPreInvoker;

    @Bean
    public ApplicationRunner runAfterStartup() {
        return args -> {
            try {
                externalPreInvoker.saveNonMajorSubjects();
                externalPreInvoker.retrieveSendNonMajor();
                externalPreInvoker.requestSseConnection();
            } catch (Exception e) {
                log.error("초기 실행 중 오류 발생: {}", e.getMessage(), e);
            }
        };
    }
}
