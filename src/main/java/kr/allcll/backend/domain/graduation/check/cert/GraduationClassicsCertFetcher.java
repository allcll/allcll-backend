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

import java.util.EnumMap;
import java.util.Map;

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

        Map<ClassicsArea, Integer> countMap = new EnumMap<>(ClassicsArea.class);

        for (Element row : table.select("tbody tr")) {
            Element th = row.selectFirst("th");
            if (th == null) {
                continue;
            }

            String label = th.text();
            Elements tds = row.select("td");
            if (tds.isEmpty()) continue;

            ClassicsArea.findByLabel(label).ifPresent(area -> {
                int actualCount = extractCompletedCount(tds.getFirst());
                countMap.put(area, actualCount);
            });
        }

        return new ClassicsCounts(
            countMap.getOrDefault(ClassicsArea.WESTERN, 0),
            countMap.getOrDefault(ClassicsArea.EASTERN, 0),
            countMap.getOrDefault(ClassicsArea.EASTERN_AND_WESTERN, 0),
            countMap.getOrDefault(ClassicsArea.SCIENCE, 0)
        );
    }

    private int extractCompletedCount(Element value) {
        try {
            String text = value.text().replace("권", "").trim();
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
