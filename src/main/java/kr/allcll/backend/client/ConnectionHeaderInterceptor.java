package kr.allcll.backend.client;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;

@Slf4j
public class ConnectionHeaderInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        String connectionHeader = response.header("Connection");
        if ("close".equalsIgnoreCase(connectionHeader)) {
            log.warn("[CONN] Connection: close. url={}", chain.request().url());
        }
        return response;
    }
}
