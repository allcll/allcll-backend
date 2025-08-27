package kr.allcll.backend.domain.seat.dto;

import kr.allcll.crawler.subject.CrawlerSubject;

public record DeprecatedSubjectSummaryResponse(
    Long subjectId,
    String subjectName,
    String subjectCode,
    String classCode,
    String department
) {

    public static DeprecatedSubjectSummaryResponse from(CrawlerSubject crawlerSubject) {
        return new DeprecatedSubjectSummaryResponse(
            crawlerSubject.getId(),
            crawlerSubject.getCuriNm(),
            crawlerSubject.getCuriNo(),
            crawlerSubject.getClassName(),
            crawlerSubject.getManageDeptNm()
        );
    }
}
