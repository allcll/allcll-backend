package kr.allcll.backend.config;


import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {
    "kr.allcll.crawler",
    "kr.allcll.backend"
})
@EntityScan(basePackages = {
    "kr.allcll.crawler",
    "kr.allcll.backend"
})
@EnableJpaRepositories(basePackages = {
    "kr.allcll.crawler",
    "kr.allcll.backend"
})
public class ModuleScanConfig {

}
