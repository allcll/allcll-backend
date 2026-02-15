package kr.allcll.backend.fixture;

import kr.allcll.crawler.subject.CrawlerSubject;

public class CrawlerSubjectFixture {

    public static CrawlerSubject createCrawlerSubject(String curiNo, String deptCd) {
        return new CrawlerSubject("", "", "", "", curiNo, "", "",
            "", "", "", "", "", "", "", "",
            "", "", "", "", deptCd, "", "",
            "", "", "", "", "", "",
            "", "", "", "", "");
    }
}
