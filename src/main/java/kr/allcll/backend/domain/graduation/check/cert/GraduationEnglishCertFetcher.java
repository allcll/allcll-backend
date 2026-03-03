package kr.allcll.backend.domain.graduation.check.cert;

import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.graduation.GraduationHtmlParser;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduationEnglishCertFetcher {

    private final LoginProperties properties;
    private final GraduationHtmlParser parser;
    private final GraduationCertDocumentFetcher documentFetcher;

    public boolean fetchEnglishPass(OkHttpClient client) {
        Document englishDoc = documentFetcher.fetch(
            client,
            properties.englishInfoPageUrl(),
            AllcllErrorCode.ENGLISH_INFO_FETCH_FAIL
        );
        return parseEnglish(englishDoc);
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
}
