package kr.allcll.backend.domain.user;

import java.io.IOException;
import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToscAuthService {

    private final LoginProperties properties;

    public void loginTosc(OkHttpClient client, LoginRequest loginRequest) {
        RequestBody body = new FormBody.Builder()
            .add("email", loginRequest.studentId())
            .add("password", loginRequest.password())
            .build();

        Request request = new Request.Builder()
            .url(properties.toscLoginUrl())
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new AllcllException(AllcllErrorCode.TOSC_LOGIN_FAIL);
            }
        } catch (IOException exception) {
            throw new AllcllException(AllcllErrorCode.TOSC_LOGIN_IO_ERROR);
        }
    }
}
