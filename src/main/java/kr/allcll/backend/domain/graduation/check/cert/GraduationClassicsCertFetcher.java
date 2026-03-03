package kr.allcll.backend.domain.graduation.check.cert;

import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsCounts;
import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsResult;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import kr.allcll.backend.support.graduation.GraduationHtmlParser;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduationClassicsCertFetcher {

    private final LoginProperties properties;
    private final GraduationHtmlParser parser;
    private final GraduationCertDocumentFetcher documentFetcher;

    public ClassicsResult fetchClassics(OkHttpClient client) {
        Document classicsDoc = documentFetcher.fetch(
            client,
            properties.studentInfoPageUrl(),
            AllcllErrorCode.CLASSIC_INFO_FETCH_FAIL
        );
        boolean classicsPass = parsePass(classicsDoc);
        ClassicsCounts classicsCounts = parseCounts(classicsDoc);
        return new ClassicsResult(classicsPass, classicsCounts);
    }

    private boolean parsePass(Document document) {
        String approvalText = parser.selectClassicsPassText(document).trim();
        return !approvalText.equals("아니오");
    }

    private ClassicsCounts parseCounts(Document document) {
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
            if (tds.isEmpty()) continue;

            int completedCount = extractCount(tds.get(0));

            if (label.contains("서양의 역사와 사상")) {
                westernCompleted = completedCount;
            }
            else if (label.contains("동양의 역사와 사상")) {
                easternCompleted = completedCount;
            }
            else if (label.contains("동·서양의 문학")) {
                literatureCompleted = completedCount;
            }
            else if (label.contains("과학 사상")) {
                scienceCompleted = completedCount;
            }
        }

        return new ClassicsCounts(westernCompleted, easternCompleted, literatureCompleted, scienceCompleted);
    }

    private int extractCount(Element value) {
        try {
            return Integer.parseInt(value.text().replace("권", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
