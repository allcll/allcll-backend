package kr.allcll.backend.admin.subject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.allcll.backend.admin.seat.dto.PinSubjectUpdateRequest;
import kr.allcll.backend.admin.seat.dto.PinSubjectUpdateRequest.PinSubject;
import kr.allcll.crawler.common.exception.CrawlerAllcllException;
import kr.allcll.crawler.subject.CrawlerSubject;
import kr.allcll.crawler.subject.CrawlerSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TargetSubjectService {

    private final TargetSubjectStorage targetSubjectStorage;
    private final SubjectFilter subjectFilter;
    private final CrawlerSubjectRepository crawlerSubjectRepository;

    public void loadPinSubjects(PinSubjectUpdateRequest request) {
        Map<CrawlerSubject, Integer> subjects = resolveSubjectWithPriority(request);
        targetSubjectStorage.addPinSubjects(subjects);
    }

    public void loadGeneralSubjects() {
        List<CrawlerSubject> targetGeneralCrawlerSubjects = subjectFilter.loadTargetGeneralSubject();
        targetSubjectStorage.addGeneralSubjects(targetGeneralCrawlerSubjects);
    }

    public void loadAllSubjects() {
        List<CrawlerSubject> targetAllCrawlerSubject = subjectFilter.loadAllSubjectToTarget();
        targetSubjectStorage.addGeneralSubjects(targetAllCrawlerSubject);
    }

    private Map<CrawlerSubject, Integer> resolveSubjectWithPriority(PinSubjectUpdateRequest request) {
        return request.subjects().stream()
            .map(subject -> Map.entry(getSubject(subject), subject.priority()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private CrawlerSubject getSubject(PinSubject subject) {
        return crawlerSubjectRepository.findById(subject.subjectId())
            .orElseThrow(() -> new CrawlerAllcllException("SUBJECT_NOT_FOUND", "과목 정보가 없습니다."));
    }
}
