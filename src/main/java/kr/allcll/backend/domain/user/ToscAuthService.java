package kr.allcll.backend.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.SocketTimeoutException;
import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.domain.user.dto.LoginRequest;
import kr.allcll.backend.domain.user.dto.ToscResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToscAuthService {

    private final LoginProperties properties;
    private final ObjectMapper objectMapper;

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
                log.error("[TOSC] HTTP 요청 실패 (상태 코드: {}, 학번: {})",
                    response.code(), loginRequest.studentId());
                return;
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                log.error("[TOSC] 응답 본문이 비어있음 (학번: {})", loginRequest.studentId());
                return;
            }

            String responseString = responseBody.string();
            ToscResponse toscResponse = objectMapper.readValue(responseString, ToscResponse.class);

            if (!toscResponse.success()) {
                log.error("[TOSC] 로그인 실패 (학번: {}, 에러: {})",
                    loginRequest.studentId(), toscResponse.getErrorMessage());
            }
        } catch (SocketTimeoutException exception) {
            log.error("[TOSC] 타임아웃 발생 (학번: {})", loginRequest.studentId(), exception);
        } catch (IOException exception) {
            log.error("[TOSC] I/O 오류 발생 (학번: {})", loginRequest.studentId(), exception);
        } catch (Exception exception) {
            log.error("[TOSC] 예상치 못한 오류 발생 (학번: {})", loginRequest.studentId(), exception);
        }
    }
}
