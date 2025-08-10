package kr.allcll.backend.config;

import java.util.Properties;
import kr.allcll.backend.client.EmailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(EmailProperties.class)
public class EmailConfig {

    private final EmailProperties emailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailProperties.host());
        mailSender.setPort(emailProperties.port());
        mailSender.setUsername(emailProperties.username());
        mailSender.setPassword(emailProperties.password());
        mailSender.setDefaultEncoding("UTF-8");
        
        Properties props = emailProperties.properties();
        if (props == null) {
            props = new Properties();
        }
        mailSender.setJavaMailProperties(props);
        return mailSender;
    }
}
