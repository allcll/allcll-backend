package kr.allcll.backend.config;

import kr.allcll.backend.client.LoginProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LoginProperties.class)
public class LoginConfig {

}
