package kr.allcll.backend.domain.graduation.check.cert;

import java.io.IOException;
import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsCounts;
import kr.allcll.backend.domain.graduation.check.cert.dto.GraduationCertInfo;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.graduation.GraduationHtmlParser;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduationCertFetcher {

    private final LoginProperties properties;
    private final GraduationHtmlParser parser;

    public GraduationCertInfo fetch(OkHttpClient client) {
        Document englishDoc = fetchDocument(client, properties.englishInfoPageUrl(),
            AllcllErrorCode.ENGLISH_INFO_FETCH_FAIL);
        Document codingDoc = fetchDocument(client, properties.codingInfoPageUrl(),
            AllcllErrorCode.CODING_INFO_FETCH_FAIL);
        Document studentDoc = fetchDocument(client, properties.studentInfoPageUrl(),
            AllcllErrorCode.CLASSIC_INFO_FETCH_FAIL);

        boolean englishPass = parseEnglish(englishDoc);
        boolean codingPass = parseCoding(codingDoc);
        boolean classicsPass = parsePass(studentDoc);
        ClassicsCounts classicsCounts = parseClassicsCounts(studentDoc);

        return GraduationCertInfo.of(
            englishPass,
            codingPass,
            classicsPass,
            classicsCounts
        );
    }

    private Document fetchDocument(OkHttpClient client, String url, AllcllErrorCode errorCode) {
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

    private boolean parseEnglish(Document document) {
        Elements resultElements = parser.selectEnglishResultRows(document);
        if (resultElements.isEmpty()) {
            return false;
        }
        for (Element row : resultElements) {
            String approvalText = row.select("td:last-child").text();
            if (approvalText.contains("승인")) {
                return true;
            }
        }
        return false;
    }

    private boolean parseCoding(Document document) {
        Elements resultElements = parser.selectCodingResultElements(document);
        return !resultElements.isEmpty();
    }

    private boolean parsePass(Document document) {
        String approvalText = parser.selectClassicsPassText(document).trim();
        return !approvalText.equals("아니오");
    }

    private ClassicsCounts parseClassicsCounts(Document document) {
        Element table = parser.selectClassicsDetailTable(document);
        if (table == null) {
            throw new AllcllException(AllcllErrorCode.CLASSIC_DETAIL_INFO_FETCH_FAIL);
        }

        int westernCompleted = 0;
        int easternCompleted = 0;
        int literatureCompleted = 0;
        int scienceCompleted = 0;

        for (Element row : table.select("tbody tr")) {
            Element th = row.selectFirst("th");
            if (th == null) {
                continue;
            }

            String label = th.text();
            Elements tds = row.select("td");
            if (tds.isEmpty()) {
                continue;
            }

            int completedCount = extractCount(tds.get(0));

            if (label.contains("서양의 역사와 사상")) {
                westernCompleted = completedCount;
            } else if (label.contains("동양의 역사와 사상")) {
                easternCompleted = completedCount;
            } else if (label.contains("동·서양의 문학")) {
                literatureCompleted = completedCount;
            } else if (label.contains("과학 사상")) {
                scienceCompleted = completedCount;
            }
        }

        return new ClassicsCounts(
            westernCompleted,
            easternCompleted,
            literatureCompleted,
            scienceCompleted
        );
    }

    private int extractCount(Element value) {
        try {
            return Integer.parseInt(value.text().replace("권", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
