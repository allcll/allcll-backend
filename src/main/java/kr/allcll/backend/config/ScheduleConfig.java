package kr.allcll.backend.config;

import io.micrometer.core.instrument.MeterRegistry;
import kr.allcll.backend.support.schedule.ScheduledTaskHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Bean
    @Qualifier("generalSeatTaskHandler")
    public ScheduledTaskHandler generalSeatTaskHandler(MeterRegistry meterRegistry) {
        return new ScheduledTaskHandler(2, "general-seat-sender", meterRegistry);
    }

    @Bean
    @Qualifier("pinSeatTaskHandler")
    public ScheduledTaskHandler pinSeatTaskHandler(MeterRegistry meterRegistry) {
        return new ScheduledTaskHandler(20, "pin-seat-sender", meterRegistry);
    }
}
