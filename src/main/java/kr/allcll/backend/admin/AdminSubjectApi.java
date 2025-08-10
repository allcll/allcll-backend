package kr.allcll.backend.admin;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import kr.allcll.backend.domain.subject.subjectReport.CrawlingMetaData;
import kr.allcll.backend.domain.subject.subjectReport.SubjectReportService;
import kr.allcll.crawler.subject.CrawlerSubjectService;
import kr.allcll.crawler.subject.SubjectSyncResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminSubjectApi {

    private final AdminRequestValidator validator;
    private final CrawlerSubjectService crawlerSubjectService;
    private final SubjectReportService subjectReportService;


    @PostMapping("/api/admin/subjects")
    public ResponseEntity<Void> syncSubjects(HttpServletRequest request,
        @RequestParam String userId,
        @RequestParam String year,
        @RequestParam String semesterCode
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        LocalDateTime startTime = LocalDateTime.now();
        SubjectSyncResult syncResult = crawlerSubjectService.syncSubjects(userId, year, semesterCode);
        LocalDateTime endTime = LocalDateTime.now();

        CrawlingMetaData metaData = new CrawlingMetaData(startTime, endTime);
        subjectReportService.generateSubjectSyncReport(syncResult, metaData);
        return ResponseEntity.ok().build();
    }
}
