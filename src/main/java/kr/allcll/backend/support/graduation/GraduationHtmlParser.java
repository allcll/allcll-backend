package kr.allcll.backend.support.graduation;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class GraduationHtmlParser {

    public Elements selectEnglishResultRows(Document document) {
        return document.select("tbody tr");
    }

    public Elements selectCodingResultElements(Document document) {
        return document.select("span.certificate:has(button)");
    }

    public String selectClassicsPassText(Document document) {
        return document.select(
            ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) " +
                "table tbody tr:has(th:contains(인증여부)) td"
        ).text();
    }

    public Element selectClassicsDetailTable(Document document) {
        return document.selectFirst(
            ".b-con-box:has(h4.b-h4-tit01:contains(영역별 인증현황)) table.b-board-table"
        );
    }
}
