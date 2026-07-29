package kr.allcll.backend.domain.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.support.exception.AllcllException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ToscAuthServiceTest {

    private MockWebServer toscServer;
    private ToscAuthService toscAuthService;
    private CookieManager portalCookieManager;

    @BeforeEach
    void setUp() throws Exception {
        toscServer = new MockWebServer();
        toscServer.start();

        String toscUrl = toscServer.url("/api/login").toString();
        LoginProperties properties = new LoginProperties(
            "https://portal.sejong.ac.kr/login",
            toscUrl,
            "https://portal.sejong.ac.kr/redirect",
            "https://portal.sejong.ac.kr",
            "https://portal.sejong.ac.kr/studentInfo",
            "https://portal.sejong.ac.kr/englishInfo",
            "https://portal.sejong.ac.kr/codingInfo"
        );
        toscAuthService = new ToscAuthService(properties, new ObjectMapper());
        portalCookieManager = new CookieManager();
        portalCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    @AfterEach
    void tearDown() throws Exception {
        toscServer.shutdown();
    }

    @Test
    @DisplayName("TOSC 로그인 실패 시에도 포털 세션 쿠키에 영향을 주지 않는다")
    void toscLoginFailureDoesNotAffectPortalCookieJar() {
        // given
        URI portalUri = URI.create("https://portal.sejong.ac.kr");
        HttpCookie sessionCookie = new HttpCookie("JSESSIONID", "portal-session-abc123");
        sessionCookie.setDomain("portal.sejong.ac.kr");
        sessionCookie.setPath("/");
        portalCookieManager.getCookieStore().add(portalUri, sessionCookie);

        List<HttpCookie> cookiesBefore = List.copyOf(
            portalCookieManager.getCookieStore().getCookies()
        );

        toscServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Set-Cookie", "TOSC_SESSION=tosc-session-xyz; Path=/; Domain=localhost")
            .setBody("{\"success\": false, \"errors\": {\"password\": \"패스워드가 일치하지 않습니다.\"}}")
        );

        LoginRequest loginRequest = new LoginRequest("22011114", "wrong-password");

        // when
        assertThatThrownBy(() -> toscAuthService.loginTosc(loginRequest))
            .isInstanceOf(AllcllException.class);

        // then
        List<HttpCookie> cookiesAfter = portalCookieManager.getCookieStore().getCookies();
        assertSoftly(softly -> {
            softly.assertThat(cookiesAfter).isEqualTo(cookiesBefore);
            softly.assertThat(cookiesAfter).hasSize(1);
            softly.assertThat(cookiesAfter.getFirst().getName()).isEqualTo("JSESSIONID");
            softly.assertThat(cookiesAfter.getFirst().getValue()).isEqualTo("portal-session-abc123");
        });
    }

    @Test
    @DisplayName("TOSC 로그인 성공 시에도 포털 세션 쿠키에 영향을 주지 않는다")
    void toscLoginSuccessDoesNotAffectPortalCookieJar() {
        // given
        URI portalUri = URI.create("https://portal.sejong.ac.kr");
        HttpCookie sessionCookie = new HttpCookie("JSESSIONID", "portal-session-abc123");
        sessionCookie.setDomain("portal.sejong.ac.kr");
        sessionCookie.setPath("/");
        portalCookieManager.getCookieStore().add(portalUri, sessionCookie);

        List<HttpCookie> cookiesBefore = List.copyOf(
            portalCookieManager.getCookieStore().getCookies()
        );

        toscServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Set-Cookie", "TOSC_SESSION=tosc-session-xyz; Path=/; Domain=localhost")
            .setBody("{\"success\": true}")
        );

        LoginRequest loginRequest = new LoginRequest("22011114", "correct-password");

        // when
        toscAuthService.loginTosc(loginRequest);

        // then
        List<HttpCookie> cookiesAfter = portalCookieManager.getCookieStore().getCookies();
        assertSoftly(softly -> {
            softly.assertThat(cookiesAfter).isEqualTo(cookiesBefore);
        });
    }
}
