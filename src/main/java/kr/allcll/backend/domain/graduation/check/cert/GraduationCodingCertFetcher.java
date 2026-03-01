package kr.allcll.backend.domain.graduation.check.cert;

import kr.allcll.backend.client.LoginProperties;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.graduation.GraduationHtmlParser;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduationCodingCertFetcher {

    private final LoginProperties properties;
    private final GraduationHtmlParser parser;
    private final GraduationCertDocumentFetcher documentFetcher;

    public boolean fetchCodingPass(OkHttpClient client) {
        Document codingDoc = documentFetcher.fetch(
            client,
            properties.codingInfoPageUrl(),
            AllcllErrorCode.CODING_INFO_FETCH_FAIL
        );
        return parseCoding(codingDoc);
    }

    private boolean parseCoding(Document document) {
        Elements resultElements = parser.selectCodingResultElements(document);
        return !resultElements.isEmpty();
    }
}
