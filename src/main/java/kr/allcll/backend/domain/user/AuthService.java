package kr.allcll.backend.domain.user;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import kr.allcll.backend.client.ConnectionEventListener;
import kr.allcll.backend.client.ConnectionHeaderInterceptor;
import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String RESULT_OK = "var result = 'OK'";
    private final LoginProperties properties;
    private final OkHttpClient loginHttpClient;

    public OkHttpClient login(LoginRequest loginRequest) {
        String studentId = loginRequest.studentId();
        try {
            String password = loginRequest.password();

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            OkHttpClient client = loginHttpClient.newBuilder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .eventListener(new ConnectionEventListener())
                .addNetworkInterceptor(new ConnectionHeaderInterceptor())
                .build();

            RequestBody body = new FormBody.Builder()
                .add("id", studentId)
                .add("password", password)
                .add("rtUrl", properties.portalLoginRedirectUrl())
                .build();

            Request request = new Request.Builder()
                .url(properties.portalLoginUrl())
                .post(body)
                .header("Referer", properties.portalLoginReferer())
                .build();

            try (Response response = client.newCall(request).execute()) {
                validateLoginResponse(studentId, response);
                log.info("[Login] 로그인 성공 (학번: {})", studentId);
                return client;
            }
        } catch (IOException exception) {
            log.error("[Login] 세종포털 통신 오류 (학번: {})", studentId, exception);
            throw new AllcllException(AllcllErrorCode.SEJONG_LOGIN_IO_ERROR, exception);
        }
    }

    private void validateLoginResponse(String studentId, Response response) throws IOException {
        if (!response.isSuccessful()) {
            log.warn("[Login] 로그인 실패 (학번: {}, 응답코드: {})", studentId, response.code());
            throw new AllcllException(AllcllErrorCode.SEJONG_LOGIN_FAIL);
        }
        // 세종대 포털은 로그인 실패에도 200을 반환하므로 응답 본문의 result 값으로 판단
        String responseBody = response.body() != null ? response.body().string() : "";
        if (responseBody.contains(RESULT_OK)) {
            return;
        }
        log.warn("[Login] 로그인 실패 (학번: {}, body: {})", studentId, responseBody);
        throw classifyLoginFailure(responseBody);
    }

    private AllcllException classifyLoginFailure(String responseBody) {
        if (responseBody.contains("개인정보 수집동의")) {
            return new AllcllException(AllcllErrorCode.SEJONG_PRIVACY_CONSENT_REQUIRED);
        }
        if (responseBody.contains("계정관리자의 요청으로 현재 로그인이 불가")) {
            return new AllcllException(AllcllErrorCode.SEJONG_ACCOUNT_LOCKED);
        }
        if (responseBody.contains("pwdNeedChg")) {
            return new AllcllException(AllcllErrorCode.SEJONG_PASSWORD_CHANGE_REQUIRED);
        }
        return new AllcllException(AllcllErrorCode.SEJONG_LOGIN_FAIL);
    }
}
