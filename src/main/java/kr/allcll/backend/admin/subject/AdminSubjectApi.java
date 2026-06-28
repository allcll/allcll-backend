package kr.allcll.backend.admin.subject;

import java.time.LocalDateTime;
import kr.allcll.backend.domain.subject.subjectReport.CrawlingMetaData;
import kr.allcll.backend.domain.subject.subjectReport.SubjectReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminSubjectApi {

    private final AdminSubjectService adminSubjectService;
    private final SubjectReportService subjectReportService;

    @PostMapping("/api/admin/subjects")
    public ResponseEntity<Void> syncSubjects(
        @RequestParam String userId,
        @RequestParam String year,
        @RequestParam String semesterCode
    ) {
        LocalDateTime startTime = LocalDateTime.now();
        SubjectSyncResult syncResult = adminSubjectService.syncSubjects(userId, year, semesterCode);
        LocalDateTime endTime = LocalDateTime.now();

        CrawlingMetaData metaData = new CrawlingMetaData(startTime, endTime);
        subjectReportService.generateSubjectSyncReport(syncResult, metaData);
        return ResponseEntity.ok().build();
    }
}
