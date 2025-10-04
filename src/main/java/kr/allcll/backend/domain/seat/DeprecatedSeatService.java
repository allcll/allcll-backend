package kr.allcll.backend.domain.seat;

import java.util.List;
import kr.allcll.backend.admin.subject.TargetSubjectStorage;
import kr.allcll.backend.domain.seat.dto.DeprecatedSubjectSummaryResponse;
import kr.allcll.crawler.subject.CrawlerSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeprecatedSeatService {

    private final TargetSubjectStorage targetSubjectStorage;

    public List<DeprecatedSubjectSummaryResponse> getAllTargetSubjects() {
        List<CrawlerSubject> targetCrawlerSubjects = targetSubjectStorage.getTargetSubjects();
        return targetCrawlerSubjects.stream()
            .map(DeprecatedSubjectSummaryResponse::from)
            .toList();
    }

    public List<DeprecatedSubjectSummaryResponse> getAllTargetGeneralSubjects() {
        List<CrawlerSubject> targetCrawlerSubjects = targetSubjectStorage.getTargetGeneralSubjects();
        return targetCrawlerSubjects.stream()
            .map(DeprecatedSubjectSummaryResponse::from)
            .toList();
    }
}
