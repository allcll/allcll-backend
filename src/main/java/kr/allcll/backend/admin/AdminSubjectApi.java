package kr.allcll.backend.admin;

import kr.allcll.crawler.subject.CrawlerSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminSubjectApi {

    private final CrawlerSubjectService crawlerSubjectService;

    @PostMapping("/api/admin/subjects")
    public void syncSubjects(
        @RequestParam String userId,
        @RequestParam String year,
        @RequestParam String semesterCode
    ) {
        crawlerSubjectService.syncSubjects(userId, year, semesterCode);
    }
}