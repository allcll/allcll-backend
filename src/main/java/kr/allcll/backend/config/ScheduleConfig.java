package kr.allcll.backend.config;

import io.micrometer.core.instrument.MeterRegistry;
import kr.allcll.backend.support.schedule.ScheduledTaskHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("pin-subjects-sent-scheduler");
        scheduler.setPoolSize(20);
        scheduler.setAwaitTerminationSeconds(15);
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    @Qualifier("generalSeatTaskHandler")
    public ScheduledTaskHandler generalSeatTaskHandler(MeterRegistry meterRegistry) {
        return new ScheduledTaskHandler(2, "general-seat-sender", meterRegistry);
    }
}
