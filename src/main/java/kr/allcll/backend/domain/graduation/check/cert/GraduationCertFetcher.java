package kr.allcll.backend.domain.graduation.check.cert;

import java.io.IOException;
import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsCounts;
import kr.allcll.backend.domain.graduation.check.cert.dto.GraduationCertInfo;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
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
        Elements resultElements = document.select("tbody tr");
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
        Elements resultElements = document.select("span.certificate");
        if (resultElements.isEmpty()) {
            return false;
        }
        for (Element result : resultElements) {
            String approvalText = result.text().trim();
            if (!approvalText.equals("불합격")) {
                return true;
            }
        }
        return false;
    }

    private boolean parsePass(Document document) {
        String approvalText = document.select(
            ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) " +
                "table tbody tr:has(th:contains(인증여부)) td"
        ).text().trim();

        return !approvalText.equals("아니오");
    }

    private ClassicsCounts parseClassicsCounts(Document document) {
        Element table = document.selectFirst(
            ".b-con-box:has(h4.b-h4-tit01:contains(영역별 인증현황)) table.b-board-table"
        );

        if (table == null) {
            throw new AllcllException(AllcllErrorCode.CLASSIC_DETAIL_INFO_FETCH_FAIL);
        }

        int westernCertRequired = 0, westernCompleted = 0;
        int easternCertRequired = 0, easternCompleted = 0;
        int literatureCertRequired = 0, literatureCompleted = 0;
        int scienceCertRequired = 0, scienceCompleted = 0;

        for (Element row : table.select("tbody tr")) {
            Element th = row.selectFirst("th");
            if (th == null) {
                continue;
            }

            String label = th.text();
            Elements tds = row.select("td");

            if (tds.size() < 2) {
                continue;
            }

            int completedCount = extractCount(tds.get(0));
            int certRequiredCount = extractCount(tds.get(1));

            if (label.contains("서양의 역사와 사상")) {
                westernCompleted = completedCount;
                westernCertRequired = certRequiredCount;
            } else if (label.contains("동양의 역사와 사상")) {
                easternCompleted = completedCount;
                easternCertRequired = certRequiredCount;
            } else if (label.contains("동·서양의 문학")) {
                literatureCompleted = completedCount;
                literatureCertRequired = certRequiredCount;
            } else if (label.contains("과학 사상")) {
                scienceCompleted = completedCount;
                scienceCertRequired = certRequiredCount;
            }
        }

        return ClassicsCounts.of(
            westernCertRequired, westernCompleted,
            easternCertRequired, easternCompleted,
            literatureCertRequired, literatureCompleted,
            scienceCertRequired, scienceCompleted
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
