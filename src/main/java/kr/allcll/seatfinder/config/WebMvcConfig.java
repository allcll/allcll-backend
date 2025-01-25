package kr.allcll.seatfinder.config;

import kr.allcll.seatfinder.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("인터셉터 동작");
        registry.addInterceptor(authInterceptor)
            .excludePathPatterns("/api/**", "/api/pin/**", "/api/subject/upload");
    }
}
