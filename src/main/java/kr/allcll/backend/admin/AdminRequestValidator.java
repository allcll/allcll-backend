package kr.allcll.backend.admin;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminRequestValidator {

    private static final String AUTH_HEADER = "X-ADMIN-TOKEN";
    private static final long REQUEST_INTERVAL_SECONDS = 5;

    private final Map<String, Long> lastRequestTimeByIp = new ConcurrentHashMap<>();

    @Value("${admin.token}")
    private String adminToken;

    public boolean isUnauthorized(HttpServletRequest request) {
        String token = request.getHeader(AUTH_HEADER);
        return !adminToken.equals(token);
    }

    public boolean isRateLimited(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        long now = Instant.now().getEpochSecond();

        Long lastTime = lastRequestTimeByIp.get(ip);
        lastRequestTimeByIp.put(ip, now);

        return lastTime != null && now - lastTime < REQUEST_INTERVAL_SECONDS;
    }
}
