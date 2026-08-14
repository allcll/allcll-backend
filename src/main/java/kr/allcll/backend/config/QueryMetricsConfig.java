package kr.allcll.backend.config;

import java.util.Map;
import java.util.Set;
import kr.allcll.backend.support.metrics.QueryMetrics;
import kr.allcll.backend.support.metrics.QueryStatsSessionListener;
import kr.allcll.backend.support.web.QueryMetricsFilter;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@ConditionalOnProperty(prefix = "allcll.metrics.query", name = "enabled", matchIfMissing = true)
public class QueryMetricsConfig {

    /*
    RequestIdFilter(HIGHEST_PRECEDENCE) 다음에 두어 집계 로그가 requestId 와 함께 남도록 한다.
     */
    private static final int QUERY_METRICS_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    @Bean
    public HibernatePropertiesCustomizer queryStatsSessionListenerCustomizer() {
        return this::registerSessionListener;
    }

    @Bean
    public FilterRegistrationBean<QueryMetricsFilter> queryMetricsFilterRegistration(QueryMetrics queryMetrics) {
        FilterRegistrationBean<QueryMetricsFilter> registration =
            new FilterRegistrationBean<>(new QueryMetricsFilter(queryMetrics));
        registration.setOrder(QUERY_METRICS_FILTER_ORDER);
        return registration;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> queryMetricsEndpointRegistrar(
        QueryMetrics queryMetrics,
        ObjectProvider<RequestMappingHandlerMapping> handlerMappings
    ) {
        return event -> handlerMappings.stream()
            .flatMap(handlerMapping -> handlerMapping.getHandlerMethods().keySet().stream())
            .flatMap(mappingInfo -> uriPatterns(mappingInfo).stream())
            .distinct()
            .forEach(queryMetrics::registerEndpoint);
    }

    private Set<String> uriPatterns(RequestMappingInfo mappingInfo) {
        PathPatternsRequestCondition pathPatterns = mappingInfo.getPathPatternsCondition();
        if (pathPatterns != null) {
            return pathPatterns.getPatternValues();
        }
        PatternsRequestCondition patterns = mappingInfo.getPatternsCondition();
        if (patterns != null) {
            return patterns.getPatterns();
        }
        return Set.of();
    }

    /*
    Hibernate 가 세션마다 인스턴스를 직접 만들기 때문에 인스턴스가 아니라 클래스 이름으로 지정한다.
     */
    private void registerSessionListener(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(
            AvailableSettings.AUTO_SESSION_EVENTS_LISTENER,
            QueryStatsSessionListener.class.getName()
        );
    }
}
