package kr.allcll.backend.admin.seat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.allcll.backend.admin.seat.dto.PinSubjectUpdateRequest;
import kr.allcll.backend.admin.seat.dto.PinSubjectUpdateRequest.PinSubject;
import kr.allcll.crawler.common.exception.CrawlerAllcllException;
import kr.allcll.crawler.subject.CrawlerSubject;
import kr.allcll.crawler.subject.CrawlerSubjectRepository;
import kr.allcll.backend.admin.subject.SubjectFilter;
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
        List<PinSubject> pinSubjects = request.subjects();
        Map<Long, CrawlerSubject> subjectsById = findSubjectsById(pinSubjects);

        return pinSubjects.stream()
            .collect(Collectors.toMap(
                pinSubject -> getSubject(subjectsById, pinSubject),
                PinSubject::priority
            ));
    }

    private Map<Long, CrawlerSubject> findSubjectsById(List<PinSubject> pinSubjects) {
        List<Long> subjectIds = pinSubjects.stream()
            .map(PinSubject::subjectId)
            .toList();

        return crawlerSubjectRepository.findAllById(subjectIds).stream()
            .collect(Collectors.toMap(CrawlerSubject::getId, Function.identity()));
    }

    private CrawlerSubject getSubject(Map<Long, CrawlerSubject> subjectsById, PinSubject subject) {
        CrawlerSubject crawlerSubject = subjectsById.get(subject.subjectId());
        if (crawlerSubject == null) {
            throw new CrawlerAllcllException("SUBJECT_NOT_FOUND", "과목 정보가 없습니다.");
        }
        return crawlerSubject;
    }
}
