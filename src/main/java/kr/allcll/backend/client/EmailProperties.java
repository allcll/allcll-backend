package kr.allcll.backend.client;

import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@ConfigurationProperties(prefix = "spring.mail")
public record EmailProperties(
    String host,
    int port,
    String username,
    String password,
    Properties properties
) {

    public EmailProperties {
        log.info("EmailProperties 접근");
    }
}