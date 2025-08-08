package kr.allcll.backend.admin;

import jakarta.servlet.http.HttpServletRequest;
import kr.allcll.crawler.subject.CrawlerSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminSubjectApi {

    private final CrawlerSubjectService crawlerSubjectService;
    private final AdminRequestValidator validator;

    @PostMapping("/api/admin/subjects")
    public ResponseEntity<Void> syncSubjects(HttpServletRequest request,
        @RequestParam String userId,
        @RequestParam String year,
        @RequestParam String semesterCode
    ) {
        if (validator.isRateLimited(request) || validator.isUnauthorized(request)) {
            return ResponseEntity.status(401).build();
        }
        crawlerSubjectService.syncSubjects(userId, year, semesterCode);
        return ResponseEntity.ok().build();
    }
}
