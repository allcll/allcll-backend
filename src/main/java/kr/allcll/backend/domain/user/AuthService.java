package kr.allcll.backend.domain.user;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
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

    private final LoginProperties properties;

    // #2 OkHttpClient 재사용: ConnectionPool과 Dispatcher를 공유하는 공용 클라이언트
    private final OkHttpClient sharedClient = new OkHttpClient();

    public OkHttpClient login(LoginRequest loginRequest) {
        try {
            String studentId = loginRequest.studentId();
            String password = loginRequest.password();

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            // #2 OkHttpClient 재사용: 공용 client에서 파생하여 connection pool 공유
            OkHttpClient client = sharedClient.newBuilder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
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
                if (!response.isSuccessful()) {
                    throw new AllcllException(AllcllErrorCode.SEJONG_LOGIN_FAIL);
                }
                return client;
            }
        } catch (IOException exception) {
            throw new AllcllException(AllcllErrorCode.SEJONG_LOGIN_IO_ERROR, exception);
        }
    }
}
