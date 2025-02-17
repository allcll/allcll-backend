package kr.allcll.seatfinder.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.runner.enabled", havingValue = "true", matchIfMissing = true)
public class ExternalConfig {

    private final ExternalPreInvoker externalPreInvoker;

    @Bean
    public ApplicationRunner runAfterStartup() {
        return args -> {
            try {
                externalPreInvoker.saveNonMajorSubjects();
                externalPreInvoker.sendSseConnectionToExternal();
            } catch (Exception e) {
                log.error("[서버] 초기 실행 중 오류 발생: {}", e.getMessage(), e);
            }
        };
    }
}
