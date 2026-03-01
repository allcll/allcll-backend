package kr.allcll.backend.domain.graduation.check.cert;

import java.io.IOException;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraduationCertDocumentFetcher {

    public Document fetch(OkHttpClient client, String url, AllcllErrorCode errorCode) {
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new AllcllException(errorCode);
            }
            String html = response.body().string();
            if (html.isBlank()) {
                throw new AllcllException(errorCode);
            }
            return Jsoup.parse(html);
        } catch (IOException exception) {
            throw new AllcllException(errorCode);
        }
    }
}
