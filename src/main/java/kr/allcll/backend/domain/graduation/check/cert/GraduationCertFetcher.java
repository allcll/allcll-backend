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
        boolean englishPass = fetchEnglish(client);
        boolean codingPass = fetchCoding(client);

        ClassicsCounts classicsCounts = fetchClassicsCounts(client);
        boolean classicsPass = fetchClassicsPass(client);

        return GraduationCertInfo.of(
            englishPass,
            codingPass,
            classicsPass,
            classicsCounts
        );
    }

    private boolean fetchEnglish(OkHttpClient client) { // 영어 인증
        Document document = fetchDocument(client, properties.englishInfoPageUrl(),
            AllcllErrorCode.ENGLISH_INFO_FETCH_FAIL);

        Elements resultElements = document.select("tbody tr");

        // 결과 없음
        if (resultElements.isEmpty()) {
            return false;
        }

        // pass 여부 검사
        for (Element row : resultElements) {
            String approvalText = row.select("td:last-child").text();
            if (approvalText.contains("승인")) {
                return true;
            }
        }
        return false;
    }

    private boolean fetchCoding(OkHttpClient client) {
        Document document = fetchDocument(client, properties.codingInfoPageUrl(),
            AllcllErrorCode.CODING_INFO_FETCH_FAIL);

        Elements resultElements = document.select("span.certificate");

        // 결과 없음
        if (resultElements.isEmpty()) {
            return false;
        }

        // pass 여부 검사
        for (Element result : resultElements) {
            String approvalText = result.text().trim();
            if (!approvalText.equals("불합격")) {
                return true;
            }
        }
        return false;
    }

    private boolean fetchClassicsPass(OkHttpClient client) { // 고전독서 인증
        Document document = fetchDocument(client, properties.studentInfoPageUrl(),
            AllcllErrorCode.CLASSIC_INFO_FETCH_FAIL);

        String approvalText = document.select(
            ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) " +
                "table tbody tr:has(th:contains(인증여부)) td"
        ).text().trim();

        // non pass 여부 검사
        if (approvalText.equals("아니오")) {
            return false;
        }
        return true;
    }

    private ClassicsCounts fetchClassicsCounts(OkHttpClient client) {
        Document document = fetchDocument(client, properties.studentInfoPageUrl(),
            AllcllErrorCode.CLASSIC_DETAIL_INFO_FETCH_FAIL);

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
            String label = row.selectFirst("th").text();
            Elements value = row.select("td");

            if (value.size() < 2) { // 정상 영역 row는 td가 2개 (이수권수, 인증권수)
                continue;
            }

            int completedCount = extractCount(value.get(0));
            int certRequiredCount = extractCount(value.get(1));

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
        int totalCompleted = westernCompleted + easternCompleted +
            literatureCompleted + scienceCompleted;
        int totalRequired = westernCertRequired + easternCertRequired +
            literatureCertRequired + scienceCertRequired;

        return new ClassicsCounts(
            totalRequired,
            totalCompleted,
            westernCertRequired, westernCompleted,
            easternCertRequired, easternCompleted,
            literatureCertRequired, literatureCompleted,
            scienceCertRequired, scienceCompleted
        );
    }

    private Document fetchDocument(OkHttpClient client, String url, AllcllErrorCode errorCode) {
        try {
            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            try (Response response = client.newCall(request).execute()) {
                return Jsoup.parse(response.body().string());
            }
        } catch (IOException exception) {
            throw new AllcllException(errorCode);
        }
    }

    private int extractCount(Element value) {
        return Integer.parseInt(value.text().replace("권", "").trim());
    }

}
