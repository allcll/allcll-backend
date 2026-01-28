package kr.allcll.backend.domain.user;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import kr.allcll.backend.client.LoginProperties;
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

    public OkHttpClient login(String id, String pw) throws IOException {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        OkHttpClient client = new OkHttpClient().newBuilder()
            .cookieJar(new JavaNetCookieJar(cookieManager))
            .build();

        RequestBody body = new FormBody.Builder()
            .add("id", id)
            .add("password", pw)
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
        }

        return client;
    }
}
