package kr.allcll.backend.admin.subject;

import java.util.List;
import kr.allcll.crawler.common.entity.CrawlerSemester;
import kr.allcll.crawler.common.properties.SjptProperties;
import kr.allcll.crawler.subject.CrawlerSubject;
import kr.allcll.crawler.subject.CrawlerSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubjectFilter {

    private final CrawlerSubjectRepository crawlerSubjectRepository;
    private final SjptProperties sjptProperties;

    public boolean includeSubject(CrawlerSubject crawlerSubject) {
        return sjptProperties.getExcludeNos().stream()
            .noneMatch(curiNo -> crawlerSubject.getCuriNo().equals(curiNo));
    }

    public boolean includeRemark(CrawlerSubject crawlerSubject) {
        if (crawlerSubject.getRemark() == null) {
            return true;
        }
        return !crawlerSubject.getRemark()
            .contains(sjptProperties.getExcludeForeignerRemark());
    }

    /*
    본 학기 때 교양을 가져옵니다.
     */
    public List<CrawlerSubject> loadTargetGeneralSubject() {
        return crawlerSubjectRepository
            .findAllByDeptCd(sjptProperties.getGeneralDeptCd(), CrawlerSemester.now())
            .stream()
            .filter(this::includeSubject)
            .filter(this::includeRemark)
            .toList();
    }

    /*
    계절 학기 때에 전체 과목을 실시간 제공하고 싶을 때에 사용합니다.
     */
    public List<CrawlerSubject> loadAllSubjectToTarget() {
        List<CrawlerSubject> crawlerSubjects = crawlerSubjectRepository.findAllBySemesterAt(CrawlerSemester.now());
        return crawlerSubjects.stream()
            .filter(this::includeSubject)
            .toList();
    }
}
