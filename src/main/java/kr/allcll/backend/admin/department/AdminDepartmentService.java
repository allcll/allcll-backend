package kr.allcll.backend.admin.department;

import java.util.List;
import kr.allcll.crawler.client.DepartmentClient;
import kr.allcll.crawler.client.model.DepartmentsResponse;
import kr.allcll.crawler.client.payload.DepartmentPayload;
import kr.allcll.crawler.credential.Credential;
import kr.allcll.crawler.credential.Credentials;
import kr.allcll.crawler.department.CrawlerDepartment;
import kr.allcll.crawler.department.CrawlerDepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDepartmentService {

    private final Credentials credentials;
    private final DepartmentClient departmentClient;
    private final CrawlerDepartmentRepository crawlerDepartmentRepository;

    public void fetchAndSaveDepartments(String userId, String year, String semesterCode) {
        Credential credential = credentials.findByUserId(userId);
        List<CrawlerDepartment> crawlerDepartments = fetchAndGetDepartment(credential, year, semesterCode);
        List<CrawlerDepartment> notDuplicatedCrawlerDepartment = filterDuplicatedDepartments(crawlerDepartments);
        saveDepartments(notDuplicatedCrawlerDepartment);
    }

    private List<CrawlerDepartment> fetchAndGetDepartment(Credential credential, String year, String semesterCode) {
        DepartmentPayload requestPayload = new DepartmentPayload(year, semesterCode);
        DepartmentsResponse response = departmentClient.execute(credential, requestPayload);
        return response.toDepartments();
    }

    private List<CrawlerDepartment> filterDuplicatedDepartments(List<CrawlerDepartment> crawlerDepartments) {
        return crawlerDepartments.stream()
            .filter(this::doesNotDuplicateDepartment)
            .toList();
    }

    /*
    중복된 Department 기준은 다음과 같다.
    1. 학과 코드로 구분한다. (deptCd)
    2. 지금까지 파악한 바로 Department은 semesterAt 조건을 고려하지 않아도 된다.
     */
    private boolean doesNotDuplicateDepartment(CrawlerDepartment crawlerDepartment) {
        return !crawlerDepartmentRepository.existsByDeptCd(crawlerDepartment.getDeptCd());
    }

    private void saveDepartments(List<CrawlerDepartment> crawlerDepartments) {
        crawlerDepartmentRepository.saveAll(crawlerDepartments);
    }
}
