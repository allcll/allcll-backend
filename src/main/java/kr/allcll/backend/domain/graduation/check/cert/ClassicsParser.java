package kr.allcll.backend.domain.graduation.check.cert;

import kr.allcll.backend.domain.graduation.check.cert.dto.ClassicsCounts;
import kr.allcll.backend.support.exception.AllcllErrorCode;
import kr.allcll.backend.support.exception.AllcllException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ClassicsParser {

    public static ClassicsCounts parseCounts(Document document) {
        Element table = document.selectFirst(
            ".b-con-box:has(h4.b-h4-tit01:contains(영역별 인증현황)) table.b-board-table"
        );

        if (table == null) {
            throw new AllcllException(AllcllErrorCode.CLASSIC_DETAIL_INFO_FETCH_FAIL);
        }

        int westernCertRequired = 0;
        int westernCompleted = 0;
        int easternCertRequired = 0;
        int easternCompleted = 0;
        int literatureCertRequired = 0;
        int literatureCompleted = 0;
        int scienceCertRequired = 0;
        int scienceCompleted = 0;

        for (Element row : table.select("tbody tr")) {
            String label = row.selectFirst("th").text();
            Elements value = row.select("td");

            if (value.size() < 2) {
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
        return ClassicsCounts.of(
            westernCertRequired, westernCompleted,
            easternCertRequired, easternCompleted,
            literatureCertRequired, literatureCompleted,
            scienceCertRequired, scienceCompleted
        );
    }

    private static int extractCount(Element value) {
        return Integer.parseInt(value.text().replace("권", "").trim());
    }
}
