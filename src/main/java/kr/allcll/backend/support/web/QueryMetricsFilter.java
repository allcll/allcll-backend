package kr.allcll.backend.support.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.allcll.backend.support.metrics.QueryMetrics;
import kr.allcll.backend.support.metrics.RequestQueryStats;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

/**
 * 요청 경계에서 DB 비용 집계를 시작·종료하고 엔드포인트별로 기록한다.
 */
@RequiredArgsConstructor
public class QueryMetricsFilter extends OncePerRequestFilter {

    private final QueryMetrics queryMetrics;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        RequestQueryStats.start();

        try {
            filterChain.doFilter(request, response);
        } finally {
            record(request, RequestQueryStats.stop());
        }
    }

    private void record(HttpServletRequest request, RequestQueryStats queryStats) {
        Object uriTemplate = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (uriTemplate == null) {
            return;
        }
        queryMetrics.record(request.getMethod(), uriTemplate.toString(), queryStats);
    }
}
