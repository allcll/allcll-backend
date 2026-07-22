package kr.allcll.backend.admin.session.sso;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.time.Duration;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.crawler.credential.Credential;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

/**
 * 관리자 계정으로 수강신청 시스템에 로그인해 크롤러가 쓸 세션을 수립한다.
 * <p>
 * 브라우저가 수강신청 페이지에 진입할 때 서버 세션에 만들어 두는 상태를 코드로 재현한다. 순서를 지켜야 하며, 특히 메뉴 로드는 권한 등록의 선행 조건이다.
 * <pre>
 * 포털 로그인 -&gt; doSsoLogin -&gt; initUserInfo -&gt; 메뉴 로드 -&gt; 프로그램별 권한 등록
 * </pre>
 * 성공 판정은 응답 값 객체에 맡긴다. 이 시스템은 실패해도 HTTP 200 과 HTML 을 돌려주고 성공 응답에도 "실패", "error" 문자열이 들어 있어, 상태 코드나 본문 문자열로는 판정할 수
 * 없다(실계정 캡처로 확인).
 * <p>
 * 비밀번호를 다루므로 요청·응답 본문을 로깅하는 클라이언트를 쓰면 안 된다. 크롤러의 loggingRestClient 를 주입하지 말 것.
 */
@Slf4j
@Component
public class SjptSsoClient {

    private static final String PORTAL_LOGIN_URL = "https://portal.sejong.ac.kr/jsp/login/login_action.jsp";
    private static final String PORTAL_LOGIN_REFERER = "https://portal.sejong.ac.kr/jsp/login/loginSSL.jsp";
    private static final String PORTAL_REFERER = "https://portal.sejong.ac.kr/";

    private static final String SJPT_BASE_URL = "https://sjpt.sejong.ac.kr";
    private static final String SSO_LOGIN_URL = SJPT_BASE_URL + "/main/view/Login/doSsoLogin.do";
    private static final String INIT_USER_INFO_URL = SJPT_BASE_URL + "/main/sys/UserInfo/initUserInfo.do";
    private static final String LIST_MENU_URL = SJPT_BASE_URL + "/main/view/Menu/doListUserMenuListLeft.do";
    private static final String INIT_USER_ROLE_URL = SJPT_BASE_URL + "/main/sys/UserRole/initUserRole.do";

    private static final String SJPT_HOST = "sjpt.sejong.ac.kr";
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";

    private static final MediaType JSON = MediaType.get("application/json; charset=\"UTF-8\"");
    private static final String MENU_REQUEST_BODY =
        "{\"dm_ReqLeftMenu\":{\"MENU_SYS_ID\":\"SELF_STUD\",\"SYSTEM_DIV\":\"SCH\",\"MENU_SYS_NM\":\"학부생학사정보\"}}";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    /**
     * 로그인부터 권한 등록까지 수행하고 크롤러가 쓸 인증 정보를 만든다. 비밀번호는 이 메서드 밖으로 나가지 않는다.
     */
    public Credential establishSession(String studentId, String password) {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new JavaNetCookieJar(cookieManager))
            .connectTimeout(TIMEOUT)
            .readTimeout(TIMEOUT)
            .build();

        loginToPortal(client, studentId, password);
        call(client, SSO_LOGIN_URL, null, PORTAL_REFERER, null);
        String jSessionId = extractSessionId(cookieManager);

        SjptUserInfoResponse userInfo = SjptUserInfoResponse.from(
            verifyAuthenticated(call(client, withAddParam(INIT_USER_INFO_URL, SjptRunContext.empty()), "",
                SJPT_BASE_URL, "mf___subMainUserInfoInit"))
        );

        SjptRunContext session = SjptRunContext.of(userInfo);
        verifyAuthenticated(call(client, withAddParam(LIST_MENU_URL, session), MENU_REQUEST_BODY,
            SJPT_BASE_URL, "mf_subUserMenuListLeft"));

        for (SjptCrawlerProgram program : SjptCrawlerProgram.values()) {
            registerProgramRole(client, session, program);
        }
        log.info("수강신청 시스템 세션을 수립했습니다: userId={}", userInfo.userId());
        return userInfo.toCredential(jSessionId);
    }

    private void loginToPortal(OkHttpClient client, String studentId, String password) {
        RequestBody form = new FormBody.Builder()
            .add("mainLogin", "Y")
            .add("id", studentId)
            .add("password", password)
            .build();
        Request request = new Request.Builder()
            .url(PORTAL_LOGIN_URL)
            .post(form)
            .header("Referer", PORTAL_LOGIN_REFERER)
            .build();
        execute(client, request);
    }

    private void registerProgramRole(OkHttpClient client, SjptRunContext session, SjptCrawlerProgram program) {
        SjptRunContext context = session.forProgram(program.getProgramKey(), program.isForceLogin());
        String body = call(client, withAddParam(INIT_USER_ROLE_URL, context), context.toJson(), SJPT_BASE_URL,
            "mf_tabMainCon_contents_" + program.getProgramKey() + "_body___subMainRoleInit");
        SjptUserRoleResponse.from(verifyAuthenticated(body)).validateRegisteredFor(program.getProgramKey());
    }

    /**
     * 잘못된 학번이나 비밀번호로도 포털과 수강신청 시스템은 200 과 세션 쿠키를 돌려준다. 인증되지 않았다는 사실은 이후 요청의 오류 봉투에서만 드러나므로 여기서 걸러낸다.
     */
    private String verifyAuthenticated(String responseBody) {
        SjptSubmitError.find(responseBody).ifPresent(error -> {
            if (error.isNotAuthenticated()) {
                throw new AllcllException(AllcllErrorCode.SEJONG_LOGIN_FAIL);
            }
            log.warn("수강신청 시스템이 요청을 거절했습니다: type={}, message={}", error.errorType(), error.message());
            throw new AllcllException(AllcllErrorCode.SSO_SESSION_ESTABLISH_FAIL);
        });
        return responseBody;
    }

    private String call(OkHttpClient client, String url, String body, String referer, String submissionId) {
        Request.Builder builder = new Request.Builder()
            .url(url)
            .header("Referer", referer);
        if (body != null) {
            builder.post(RequestBody.create(body, JSON))
                .header("Accept", "application/json")
                .header("Origin", SJPT_BASE_URL);
        }
        if (submissionId != null) {
            builder.header("submissionid", submissionId);
        }
        return execute(client, builder.build());
    }

    private String execute(OkHttpClient client, Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                throw new AllcllException(AllcllErrorCode.SSO_SESSION_ESTABLISH_FAIL);
            }
            return response.body().string();
        } catch (IOException exception) {
            throw new AllcllException(AllcllErrorCode.SSO_SESSION_ESTABLISH_FAIL, exception);
        }
    }

    /**
     * 로그인 성공 여부는 본문으로 판정할 수 없으므로, 수강신청 시스템의 세션 쿠키가 발급되었는지로 확인한다.
     */
    private String extractSessionId(CookieManager cookieManager) {
        return cookieManager.getCookieStore().getCookies().stream()
            .filter(cookie -> SESSION_COOKIE_NAME.equals(cookie.getName()))
            .filter(cookie -> cookie.getDomain() != null && cookie.getDomain().contains(SJPT_HOST))
            .map(HttpCookie::getValue)
            .findFirst()
            .orElseThrow(() -> new AllcllException(AllcllErrorCode.SSO_SESSION_ESTABLISH_FAIL));
    }

    private String withAddParam(String url, SjptRunContext context) {
        return url + "?addParam=" + context.toAddParam();
    }
}
