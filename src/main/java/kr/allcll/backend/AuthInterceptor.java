package kr.allcll.backend;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import kr.allcll.backend.exception.AllcllErrorCode;
import kr.allcll.backend.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(CookieProperties.class)
public class AuthInterceptor implements HandlerInterceptor {

    private static final String TOKEN_KEY = "token";

    private final CookieProperties cookieProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (hasNoToken(request)) {
            String token = TokenProvider.create();
            ThreadLocalHolder.SHARED_TOKEN.set(token);
            response.addCookie(createCookie(token));
            return true;
        }
        ThreadLocalHolder.SHARED_TOKEN.set(findTokenFromCookie(request));
        return true;
    }

    private boolean hasNoToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return true;
        }
        return Arrays.stream(request.getCookies())
            .noneMatch(cookie -> TOKEN_KEY.equals(cookie.getName()));
    }

    private String findTokenFromCookie(HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
            .filter(cookie -> TOKEN_KEY.equals(cookie.getName()))
            .findAny()
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.TOKEN_NOT_FOUND))
            .getValue();
    }

    private Cookie createCookie(String token) {
        Cookie cookie = new Cookie(TOKEN_KEY, token);
        cookie.setPath(cookieProperties.path());
        cookie.setDomain(cookieProperties.domain());
        cookie.setSecure(cookieProperties.secure());
        cookie.setHttpOnly(cookieProperties.httpOnly());
        cookie.setMaxAge((int) cookieProperties.maxAge().getSeconds());
        return cookie;
    }
}
