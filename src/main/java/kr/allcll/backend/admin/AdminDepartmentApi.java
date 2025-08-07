package kr.allcll.backend.admin;

import kr.allcll.crawler.department.CrawlerDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminDepartmentApi {

    private final CrawlerDepartmentService crawlerDepartmentService;

    @PostMapping("/api/admin/departments/fetch")
    public void fetchAndSaveDepartments(
        @RequestParam String userId,
        @RequestParam String year,
        @RequestParam String semesterCode
    ) {
        crawlerDepartmentService.fetchAndSaveDepartments(userId, year, semesterCode);
    }
}
