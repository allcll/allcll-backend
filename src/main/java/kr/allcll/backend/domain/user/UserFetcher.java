package kr.allcll.backend.domain.user;

import java.io.IOException;
import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFetcher {

    private final LoginProperties properties;

    public UserInfo fetch(OkHttpClient client) throws IOException {
        Request request = new Request.Builder()
            .url(properties.studentInfoPageUrl())
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new AllcllException(AllcllErrorCode.USER_INFO_FETCH_FAIL);
            }

            Document doc = Jsoup.parse(response.body().string());

            return parseUserInfo(doc);
        }
    }

    private UserInfo parseUserInfo(Document doc) {
        String selector =
            ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";

        String studentId = null;
        String name = null;
        String dept = null;

        for (var row : doc.select(selector)) {
            String th = row.select("th").text().trim();
            String td = row.select("td").text().trim();

            switch (th) {
                case "학번" -> studentId = td;
                case "이름" -> name = td;
                case "학과명" -> dept = td;
            }
        }

        if (studentId == null || name == null || dept == null) {
            throw new AllcllException(AllcllErrorCode.USER_INFO_FETCH_FAIL);
        }

        return UserInfo.of(studentId, name, dept);
    }
}
