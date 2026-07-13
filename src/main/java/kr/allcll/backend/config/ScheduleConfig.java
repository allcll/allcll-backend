package kr.allcll.backend.config;

import io.micrometer.core.instrument.MeterRegistry;
import kr.allcll.backend.support.metrics.SeatPipelineMetrics;
import kr.allcll.backend.support.scheduler.ScheduledTaskHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Bean
    @Qualifier("generalSeatTaskHandler")
    public ScheduledTaskHandler generalSeatTaskHandler(
        MeterRegistry meterRegistry,
        SeatPipelineMetrics seatPipelineMetrics
    ) {
        return new ScheduledTaskHandler(1, "general-seat-sender", meterRegistry, seatPipelineMetrics);
    }

    @Bean
    @Qualifier("pinSeatTaskHandler")
    public ScheduledTaskHandler pinSeatTaskHandler(
        MeterRegistry meterRegistry,
        SeatPipelineMetrics seatPipelineMetrics
    ) {
        return new ScheduledTaskHandler(20, "pin-seat-sender", meterRegistry, seatPipelineMetrics);
    }
}
