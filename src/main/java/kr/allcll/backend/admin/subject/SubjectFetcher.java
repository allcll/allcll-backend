package kr.allcll.backend.admin.subject;

import java.util.List;
import kr.allcll.crawler.client.SubjectClient;
import kr.allcll.crawler.client.model.SubjectsResponse;
import kr.allcll.crawler.client.payload.SubjectPayload;
import kr.allcll.crawler.credential.Credential;
import kr.allcll.crawler.credential.Credentials;
import kr.allcll.crawler.department.CrawlerDepartment;
import kr.allcll.crawler.department.CrawlerDepartmentRepository;
import kr.allcll.crawler.subject.CrawlerSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubjectFetcher {

    private final Credentials credentials;
    private final SubjectClient subjectClient;
    private final CrawlerDepartmentRepository crawlerDepartmentRepository;

    public List<CrawlerSubject> fetchSubjects(String userId, String year, String semesterCode) {
        Credential credential = credentials.findByUserId(userId);
        List<CrawlerDepartment> crawlerDepartments = crawlerDepartmentRepository.findAll();

        return crawlerDepartments.stream()
            .map(department -> fetchAndGetSubjects(credential, year, semesterCode,
                department.getDeptCd()))
            .flatMap(List::stream)
            .toList();
    }

    private List<CrawlerSubject> fetchAndGetSubjects(
        Credential credential,
        String year,
        String semesterCode,
        String deptCd
    ) {
        SubjectPayload requestPayload = new SubjectPayload(year, semesterCode, deptCd);
        SubjectsResponse response = subjectClient.execute(credential, requestPayload);
        if (response.subjectResponses() == null) {
            log.error("[SubjectFetcher] SubjectCourses Fail: {}", response);
            return List.of();
        }
        return response.toSubjects();
    }
}
