package kr.allcll.backend.config;

import java.util.concurrent.TimeUnit;
import kr.allcll.backend.client.ConnectionEventListener;
import kr.allcll.backend.client.ConnectionHeaderInterceptor;
import kr.allcll.backend.client.LoginProperties;
import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LoginProperties.class)
public class LoginConfig {

    @Bean
    public OkHttpClient loginHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(10, 2, TimeUnit.MINUTES);
        return new OkHttpClient.Builder()
            .connectionPool(connectionPool)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .addNetworkInterceptor(new ConnectionHeaderInterceptor())
            .eventListener(new ConnectionEventListener())
            .build();
    }
}
