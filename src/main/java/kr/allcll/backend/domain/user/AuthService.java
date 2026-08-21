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
import okhttp3.FormBody;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String RESULT_OK = "var result = 'OK'";
    private final LoginProperties properties;
    private final OkHttpClient loginHttpClient;

    public OkHttpClient login(LoginRequest loginRequest) {
        try {
            String studentId = loginRequest.studentId();
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
                if (!isSuccessful(response)) {
                    throw new AllcllException(AllcllErrorCode.SEJONG_LOGIN_FAIL);
                }
                return client;
            }
        } catch (IOException exception) {
            throw new AllcllException(AllcllErrorCode.SEJONG_LOGIN_IO_ERROR, exception);
        }
    }

    private boolean isSuccessful(Response response) throws IOException {
        if (!response.isSuccessful()) {
            return false;
        }
        // 세종대 포털은 로그인 실패에도 200을 반환하므로 응답 본문의 result 값으로 판단
        String responseBody = response.body() != null ? response.body().string() : "";
        return responseBody.contains(RESULT_OK);
    }
}
